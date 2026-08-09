package pl.kamjer.shoppinglistservice.config.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;
import pl.kamjer.shoppinglistservice.config.websocket.Topic;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Configuration
public class WebSocketBeanConfig {

    @Bean
    public ConcurrentHashMap<String, WebSocketSession> concurrentHashMapStringWebSocketSession() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public ConcurrentHashMap<String, HashMap<String, WebSocketSession>> concurrentHashMapStringHashMapStringWebsocket() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public ThreadLocal<WebSocketSession> threadLocalWebSocketSession() {
        return new ThreadLocal<>();
    }

    @Bean
    public CopyOnWriteArrayList<Topic> copyOnWriteArrayListTopic () {
        return new CopyOnWriteArrayList<>();
    }

    @Bean
    public CopyOnWriteArrayList<String> copyOnWriteArrayListString() {
        return new CopyOnWriteArrayList<>();
    }
}
