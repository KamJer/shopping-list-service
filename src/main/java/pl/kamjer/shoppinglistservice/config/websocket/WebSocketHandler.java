package pl.kamjer.shoppinglistservice.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Log4j2
public class WebSocketHandler implements org.springframework.web.socket.WebSocketHandler {

    private final MessageValidator messageValidator;
    private final WebSocketDataHolder webSocketDataHolder;
    private final ConnectionBroker connectionBroker;
    private final WebsocketMessageDecryptor websocketMessageDecryptor;

    private final Map<String, StringBuilder> partialMessagesMap = new HashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("New session connected: {}, user: {}", session.getId(), Optional.ofNullable(session.getPrincipal()).orElseThrow().getName());
        webSocketDataHolder.putSessionConnected(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            webSocketDataHolder.setCurrentSession(session);
            if (message.getPayload() instanceof String payload) {
                StringBuilder partialMessage = partialMessagesMap.computeIfAbsent(session.getId(), k -> new StringBuilder());
                partialMessage.append(payload);

                if (message.isLast()) {
                    String fullMessage = partialMessage.toString();
                    log.info("New message from user: {}", Optional.ofNullable(session.getPrincipal()).orElseThrow().getName());

                    Message protocolMessage = websocketMessageDecryptor.decipher(fullMessage);
                    if (!messageValidator.validateMessage(protocolMessage)) {
                        throw new IllegalArgumentException("Wrong header type for send message");
                    }

                    switch (protocolMessage.getCommand()) {
                        case CONNECT -> connectionBroker.handleConnect(session);
                        case MESSAGE -> connectionBroker.handleMessage(session, protocolMessage);
                        case SUBSCRIBE -> connectionBroker.handleSubscribe(session, protocolMessage);
                        case UNSUBSCRIBE -> connectionBroker.handleUnsubscribe(session, protocolMessage);
                    }
                    partialMessagesMap.remove(session.getId());
                }
            }
        } catch (Exception e) {
            if (e instanceof InvocationTargetException iE) {
                log.error(iE.getCause());
                connectionBroker.handleException(session, iE.getCause());
            } else {
                log.error(e);
                connectionBroker.handleException(session, e);
            }
        } finally {
            log.info("End of message processing - session: {} from: {}", session.getId(), session.getPrincipal());
            webSocketDataHolder.clearCurrentSession();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Error {} - session: {} from: {}", exception.getMessage(), session.getId(), session.getPrincipal());
//        connectionBroker.handleException(session, exception);
        webSocketDataHolder.removeSessionFromTopics(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("Session closing: {} - reason: {}", session.getId(), closeStatus.getReason());
        webSocketDataHolder.removeSessionFromTopics(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    public void registerTopic(String... topic) {
        webSocketDataHolder.registerTopic(topic);
    }
}
