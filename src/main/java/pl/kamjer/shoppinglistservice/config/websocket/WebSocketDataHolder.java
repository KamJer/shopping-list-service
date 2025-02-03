package pl.kamjer.shoppinglistservice.config.websocket;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class WebSocketDataHolder {

    private final ThreadLocal<WebSocketSession> currentSession;

    private  final ConcurrentHashMap<String, WebSocketSession> sessionsConnected;

    // making value of map a map makes it impossible for creating double subscriptions from one session on one topic
    private  final ConcurrentHashMap<String, HashMap<String, WebSocketSession>> subscribedTopicsAndSessions;

    private final CopyOnWriteArrayList<Topic> parameterTopics;
    private final CopyOnWriteArrayList<String> basicTopics;

    public WebSocketDataHolder() {
        this.sessionsConnected = new ConcurrentHashMap<>();
        this.subscribedTopicsAndSessions = new ConcurrentHashMap<>();
        this.currentSession = new ThreadLocal<>();
        this.parameterTopics = new CopyOnWriteArrayList<>();
        this.basicTopics = new CopyOnWriteArrayList<>();
    }

    void menageParameterTopics(Topic baseUrl, String topic, WebSocketSession session) {
        if (parameterTopics.contains(baseUrl) && !subscribedTopicsAndSessions.containsKey(topic)) {
            this.subscribedTopicsAndSessions.put(topic, new HashMap<>());
            addSessionToTopic(topic, session);
        } else {
            addSessionToTopic(topic, session);
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
                parameterTopics.add(new Topic(topic, parameterCount));
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
     * @param topic - topic to subscribe to
     * @param session - session for subscription
     */
    void addSessionToTopic(String topic, WebSocketSession session){
        getSessionsForTopic(topic).put(session.getId(), session);
    }

    /**
     * removes sessions for topics
     * @param topic - topic to delete session from
     * @param session - session to remove
     */
    void removeSubscription(String topic, WebSocketSession session) {
        getSessionsForTopic(topic).remove(session);
    }

    public HashMap<String, WebSocketSession> getSessionsForTopic(String topic) {
        return Optional.ofNullable(subscribedTopicsAndSessions.get(topic)).orElseThrow();
    }

    public void removeSessionFromTopics(WebSocketSession session) {
        sessionsConnected.remove(session.getId());
        subscribedTopicsAndSessions.forEach((key, value) -> {
            value.remove(session);
        });
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
