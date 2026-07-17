package pl.kamjer.shoppinglistservice.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionBrokerTest {

    @Mock
    private BeanInspector beanInspector;
    @Mock
    private WebSocketDataHolder webSocketDataHolder;
    @Mock
    private WebsocketMessageDecryptor websocketMessageDecryptor;
    @Mock
    private WebSocketSession session;

    private ConnectionBroker broker;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        broker = new ConnectionBroker(beanInspector, webSocketDataHolder, websocketMessageDecryptor);
    }

    @Test
    void handleConnect_sendsConnectedMessage() throws IOException {
        when(session.getId()).thenReturn("sess-1");
        when(websocketMessageDecryptor.jsonphyMessage(any(Message.class))).thenReturn("{}");

        broker.handleConnect(session);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(websocketMessageDecryptor).jsonphyMessage(captor.capture());
        assertThat(captor.getValue().getCommand()).isEqualTo(Message.Command.CONNECTED);
        assertThat(captor.getValue().getHeaders().get(Message.Header.ID)).isEqualTo("sess-1");

        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void handleMessage_callsBeanInspectorAndBroadcasts() throws Exception {
        when(session.getId()).thenReturn("sess-1");

        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.ID, "sess-1");
        headers.put(Message.Header.DEST, "/{userName}/putCategory");
        headers.put(Message.Header.BODY, "{\"typeName\":\"test\"}");
        headers.put(Message.Header.PARA, "alice");
        Message message = new Message(Message.Command.MESSAGE, headers);

        Topic expectedTopic = new Topic("/{userName}/putCategory", new String[]{"alice"});
        when(beanInspector.findControllerMethodAndCall(eq(expectedTopic), any(String[].class)))
                .thenReturn(Optional.of("{\"categoryId\":1}"));
        lenient().when(websocketMessageDecryptor.jsonphyMessage(any(Message.class))).thenReturn("{}");

        WebSocketSession otherSession = mock(WebSocketSession.class);
        HashMap<String, WebSocketSession> sessions = new HashMap<>();
        sessions.put("sess-1", session);
        sessions.put("sess-2", otherSession);
        when(webSocketDataHolder.getSessionsForTopic(expectedTopic)).thenReturn(sessions);

        broker.handleMessage(session, message);

        verify(session).sendMessage(any(TextMessage.class));
        verify(otherSession).sendMessage(any(TextMessage.class));
    }

    @Test
    void handleMessage_nullResult_doesNotBroadcast() throws Exception {
        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.ID, "sess-1");
        headers.put(Message.Header.DEST, "/synchronizeData");
        headers.put(Message.Header.BODY, "");
        Message message = new Message(Message.Command.MESSAGE, headers);

        Topic topic = new Topic("/synchronizeData", new String[]{});
        when(beanInspector.findControllerMethodAndCall(eq(topic), any(String[].class)))
                .thenReturn(Optional.empty());

        broker.handleMessage(session, message);

        verify(session, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void handleSubscribe_basicTopic_addsSessionToTopic() throws Exception {
        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.DEST, "/synchronizeData");
        Message message = new Message(Message.Command.SUBSCRIBE, headers);

        when(websocketMessageDecryptor.jsonphyMessage(any(Message.class))).thenReturn("{}");

        broker.handleSubscribe(session, message);

        Topic topic = new Topic("/synchronizeData", new String[]{});
        verify(webSocketDataHolder).addSessionToTopic(topic, session);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(websocketMessageDecryptor).jsonphyMessage(captor.capture());
        assertThat(captor.getValue().getCommand()).isEqualTo(Message.Command.SUBSCRIBED);
        assertThat(captor.getValue().getHeaders().get(Message.Header.DEST)).isEqualTo("/synchronizeData");

        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void handleSubscribe_parameterizedTopic_createsConcreteTopic() throws Exception {
        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.DEST, "/{userName}/pip");
        headers.put(Message.Header.PARA, "alice");
        Message message = new Message(Message.Command.SUBSCRIBE, headers);

        when(websocketMessageDecryptor.jsonphyMessage(any(Message.class))).thenReturn("{}");

        broker.handleSubscribe(session, message);

        Topic parameterized = new Topic("/{userName}/pip", new String[]{"alice"});
        verify(webSocketDataHolder).menageParameterTopics(parameterized, session);
        verify(webSocketDataHolder).addSessionToTopic(parameterized, session);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(websocketMessageDecryptor).jsonphyMessage(captor.capture());
        assertThat(captor.getValue().getHeaders().get(Message.Header.DEST)).isEqualTo("/alice/pip");

        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void handleSubscribe_wrongParameterCount_throws() {
        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.DEST, "/{userName}/pip");
        headers.put(Message.Header.PARA, "alice;extra");
        Message message = new Message(Message.Command.SUBSCRIBE, headers);

        assertThatThrownBy(() -> broker.handleSubscribe(session, message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Wrong amount of parameters");
    }

    @Test
    void handleUnsubscribe_removesSessionAndSendsConfirmation() throws Exception {
        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.DEST, "/synchronizeData");
        headers.put(Message.Header.PARA, "");
        Message message = new Message(Message.Command.UNSUBSCRIBE, headers);

        when(websocketMessageDecryptor.jsonphyMessage(any(Message.class))).thenReturn("{}");

        broker.handleUnsubscribe(session, message);

        Topic topic = new Topic("/synchronizeData", new String[]{});
        verify(webSocketDataHolder).removeSubscription(topic, session);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(websocketMessageDecryptor).jsonphyMessage(captor.capture());
        assertThat(captor.getValue().getCommand()).isEqualTo(Message.Command.UNSUBSCRIBED);

        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void handleException_sessionOpen_sendsErrorMessage() throws Exception {
        when(session.isOpen()).thenReturn(true);
        when(websocketMessageDecryptor.jsonphyMessage(any(Message.class))).thenReturn("{}");

        broker.handleException(session, new RuntimeException("test error"));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(websocketMessageDecryptor).jsonphyMessage(captor.capture());
        assertThat(captor.getValue().getCommand()).isEqualTo(Message.Command.ERROR);
        assertThat(captor.getValue().getHeaders().get(Message.Header.BODY)).isEqualTo("test error");

        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void handleException_sessionClosed_doesNotSend() throws Exception {
        when(session.isOpen()).thenReturn(false);

        broker.handleException(session, new RuntimeException("test error"));

        verify(session, never()).sendMessage(any(TextMessage.class));
        verify(websocketMessageDecryptor, never()).jsonphyMessage(any(Message.class));
    }
}
