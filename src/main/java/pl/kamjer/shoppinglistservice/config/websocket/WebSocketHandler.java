package pl.kamjer.shoppinglistservice.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Log4j2
public class WebSocketHandler implements org.springframework.web.socket.WebSocketHandler {

    private final MessageValidator messageValidator;
    private final WebSocketDataHolder webSocketDataHolder;
    private final ConnectionBroker connectionBroker;
    private final WebsocketMessageDecryptor websocketMessageDecryptor;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New session connected: {}, user: {}", session.getId(), Optional.ofNullable(session.getPrincipal()).orElseThrow().getName());
        webSocketDataHolder.putSessionConnected(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            webSocketDataHolder.setCurrentSession(session);
            if (message.getPayload() instanceof String payload) {
                log.info("New message - user: {} /n {}", Optional.ofNullable(session.getPrincipal()).orElseThrow().getName(), message.getPayload());
                Message protocolMessage = websocketMessageDecryptor.decipher(payload);
                if (!messageValidator.validateMessage(protocolMessage)) {
                    throw new IllegalArgumentException("Wrong header type for send message");
                }

                switch (protocolMessage.getCommand()) {
                    case CONNECT -> connectionBroker.handleConnect(session);
                    case MESSAGE -> connectionBroker.handleMessage(session, protocolMessage);
                    case SUBSCRIBE -> connectionBroker.handleSubscribe(session, protocolMessage);
                    case UNSUBSCRIBE -> connectionBroker.handleUnsubscribe(session, protocolMessage);
                }
            }
        } catch (Exception e) {
            connectionBroker.handleException(session, e);
        } finally {
            webSocketDataHolder.clearCurrentSession();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        connectionBroker.handleException(session, exception);
        webSocketDataHolder.removeSessionFromTopics(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("Session closing: {} - reason: {}", session.getId(), closeStatus.getReason());
        webSocketDataHolder.removeSessionFromTopics(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void registerTopic(String... topic) {
        webSocketDataHolder.registerTopic(topic);
    }
}
