package pl.kamjer.shoppinglistservice.config.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebSocketDataHolderTest {

    private WebSocketDataHolder holder;
    private ConcurrentHashMap<String, WebSocketSession> sessionsConnected;
    private ConcurrentHashMap<String, HashMap<String, WebSocketSession>> subscribedTopicsAndSessions;

    @BeforeEach
    void setUp() {
        sessionsConnected = new ConcurrentHashMap<>();
        subscribedTopicsAndSessions = new ConcurrentHashMap<>();
        holder = new WebSocketDataHolder(
                sessionsConnected,
                subscribedTopicsAndSessions,
                new ThreadLocal<>(),
                new CopyOnWriteArrayList<>(),
                new CopyOnWriteArrayList<>()
        );
    }

    @Test
    void basicTopic_registerAndSubscribe_putsSessionUnderTopicKey() {
        holder.registerTopic("/synchronizeData");
        WebSocketSession session = session("sess-1");

        holder.addSessionToTopic(new Topic("/synchronizeData", new String[]{}), session);

        assertThat(holder.getSessionsForTopic(new Topic("/synchronizeData", new String[]{})))
                .containsEntry("sess-1", session);
    }

    @Test
    void parameterizedTopic_menageParameterTopics_createsConcreteUrlAndSubscribes() {
        holder.registerTopic("/{userName}/pip");
        WebSocketSession session = session("sess-a");

        holder.menageParameterTopics(new Topic("/{userName}/pip", new String[]{"alice"}), session);

        Topic concrete = new Topic("/{userName}/pip", new String[]{"alice"});
        assertThat(holder.getSessionsForTopic(concrete)).containsEntry("sess-a", session);
        assertThat(subscribedTopicsAndSessions).containsKey("/alice/pip");
    }

    @Test
    void disconnect_secondClientRemainsOnParameterizedTopic_otherKeepsSubscription() {
        holder.registerTopic("/{userName}/pip");
        WebSocketSession first = session("first");
        WebSocketSession second = session("second");
        Topic sub = new Topic("/{userName}/pip", new String[]{"bob"});

        holder.menageParameterTopics(sub, first);
        holder.menageParameterTopics(sub, second);

        assertThat(holder.getSessionsForTopic(sub)).hasSize(2);

        holder.removeSessionFromTopics(first);

        assertThat(holder.getSessionsForTopic(sub))
                .containsOnlyKeys("second")
                .containsEntry("second", second);
        assertThat(subscribedTopicsAndSessions).containsKey("/bob/pip");
    }

    @Test
    void disconnect_lastClientOnParameterizedTopic_removesTopicEntry() {
        holder.registerTopic("/{userName}/pip");
        WebSocketSession session = session("only");
        Topic sub = new Topic("/{userName}/pip", new String[]{"carol"});

        holder.menageParameterTopics(sub, session);
        assertThat(subscribedTopicsAndSessions).containsKey("/carol/pip");

        holder.removeSessionFromTopics(session);

        assertThat(subscribedTopicsAndSessions).doesNotContainKey("/carol/pip");
        assertThatThrownBy(() -> holder.getSessionsForTopic(sub))
                .isInstanceOf(NoResourcesFoundException.class)
                .hasMessageContaining("No such topic exists");
    }

    @Test
    void disconnect_allClientsOnBasicTopic_leavesEmptySubscriberMapButKeyRemains() {
        holder.registerTopic("/synchronizeData");
        WebSocketSession a = session("a");
        WebSocketSession b = session("b");
        Topic basic = new Topic("/synchronizeData", new String[]{});

        holder.addSessionToTopic(basic, a);
        holder.addSessionToTopic(basic, b);

        holder.removeSessionFromTopics(a);
        holder.removeSessionFromTopics(b);

        assertThat(subscribedTopicsAndSessions).containsKey("/synchronizeData");
        assertThat(holder.getSessionsForTopic(basic)).isEmpty();
    }

    @Test
    void putSessionConnected_removeSessionFromTopics_dropsFromConnectedMap() {
        WebSocketSession s = session("x");
        holder.putSessionConnected(s);
        assertThat(sessionsConnected).containsKey("x");

        holder.removeSessionFromTopics(s);

        assertThat(sessionsConnected).doesNotContainKey("x");
    }

    private static WebSocketSession session(String id) {
        WebSocketSession mock = mock(WebSocketSession.class);
        when(mock.getId()).thenReturn(id);
        return mock;
    }
}
