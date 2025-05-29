package pl.kamjer.shoppinglistservice.config.websocket;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class WebSocketDataHolder {

    private final ThreadLocal<WebSocketSession> currentSession;

    private final ConcurrentHashMap<String, WebSocketSession> sessionsConnected;

    // making value of map a map makes it impossible for creating double subscriptions from one session on one topic
    private final ConcurrentHashMap<String, HashMap<String, WebSocketSession>> subscribedTopicsAndSessions;

    private final CopyOnWriteArrayList<Topic> parameterTopics;
    private final CopyOnWriteArrayList<String> basicTopics;

    public WebSocketDataHolder(ConcurrentHashMap<String, WebSocketSession> sessionsConnected,
                               ConcurrentHashMap<String, HashMap<String, WebSocketSession>> subscribedTopicsAndSessions,
                               ThreadLocal<WebSocketSession> currentSession,
                               CopyOnWriteArrayList<Topic> parameterTopics,
                               CopyOnWriteArrayList<String> basicTopics) {
        this.sessionsConnected = sessionsConnected;
        this.subscribedTopicsAndSessions = subscribedTopicsAndSessions;
        this.currentSession = currentSession;
        this.parameterTopics = parameterTopics;
        this.basicTopics = basicTopics;
    }

    void menageParameterTopics(Topic baseUrl, WebSocketSession session) {
        if (parameterTopics.contains(baseUrl) && !subscribedTopicsAndSessions.containsKey(baseUrl.getParameterizedUrl())) {
            this.subscribedTopicsAndSessions.put(baseUrl.getParameterizedUrl(), new HashMap<>());
            addSessionToTopic(baseUrl, session);
        } else {
            addSessionToTopic(baseUrl, session);
        }
    }

    public void registerTopic(String... topics) {
//        looping through all passed topics
        for (String topic : topics) {
//            checking if topic has parameters
            int parameterCount = StringUtils.countOccurrencesOf(topic, "{");
            if (parameterCount > 0) {
//                if it does check if they are correct
                int parameterEnds = StringUtils.countOccurrencesOf(topic, "}");
                if (parameterCount != parameterEnds) {
                    throw new IllegalArgumentException("Wrong parameter definition");
                }
//                add to the list of parameterTopics to look through later in creation of them (those topics are created when subscribed)
                parameterTopics.add(new Topic(topic, new String[parameterCount]));
            } else {
//                if topic does not have parameters add to the list of registered topics
                this.subscribedTopicsAndSessions.put(topic, new HashMap<>());
                this.basicTopics.add(topic);
            }
        }
    }

    void putSessionConnected(WebSocketSession webSocketSession) {
        sessionsConnected.put(webSocketSession.getId(), webSocketSession);
    }

    /**
     * Adds sessions to registered topic
     *
     * @param topic   - topic to subscribe to
     * @param session - session for subscription
     */
    void addSessionToTopic(Topic topic, WebSocketSession session) {
        getSessionsForTopic(topic).put(session.getId(), session);
    }

    /**
     * removes sessions for topic
     *
     * @param topic   - topic to delete session from
     * @param session - session to remove
     */
    void removeSubscription(Topic topic, WebSocketSession session) {
        getSessionsForTopic(topic).remove(session.getId());
    }

    public HashMap<String, WebSocketSession> getSessionsForTopic(Topic topic) {
        Optional<HashMap<String, WebSocketSession>> optional = Optional.ofNullable(subscribedTopicsAndSessions.get(topic.getParameterizedUrl()));
        return optional.orElseThrow(() -> new NoResourcesFoundException("No such topic exists"));
    }

    public void removeSessionFromTopics(WebSocketSession session) {
        sessionsConnected.remove(session.getId());
        subscribedTopicsAndSessions.forEach((key, value) -> value.remove(session.getId()));
        subscribedTopicsAndSessions.entrySet()
                .stream()
                .filter(stringListEntry -> !stringListEntry.getValue().isEmpty() && !basicTopics.contains(stringListEntry.getKey()))
                .map(Map.Entry::getKey)
                .forEach(subscribedTopicsAndSessions::remove);

    }

    public WebSocketSession getCurrentSession() {
        return currentSession.get();
    }

    void setCurrentSession(WebSocketSession session) {
        this.currentSession.set(session);
    }

    void clearCurrentSession() {
        this.currentSession.remove();
    }


}
