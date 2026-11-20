package pl.kamjer.shoppinglistservice.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Log4j2
public class WebSocketHandler implements org.springframework.web.socket.WebSocketHandler {

    private final MessageValidator messageValidator;
    private final WebSocketDataHolder webSocketDataHolder;
    private final ConnectionBroker connectionBroker;
    private final WebsocketMessageDecryptor websocketMessageDecryptor;

    private final ConcurrentHashMap<String, StringBuilder> partialMessagesMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("New session connected: {}, user: {}", session.getId(), Optional.ofNullable(session.getPrincipal()).map(Principal::getName).orElse(""));
        webSocketDataHolder.putSessionConnected(session);
        String token = resolveToken(session);
        if (token != null) {
            session.getAttributes().put(WebSocketTokenHandshakeInterceptor.TOKEN_ATTRIBUTE, token);
        }
    }

//    The token can arrive through two channels: the Authorization header on the handshake
//    (native clients, e.g. Android) or the "token" query parameter (browsers, which cannot set
//    custom handshake headers). The SecurityContext is cleared before this callback, so the
//    primary source is the attribute captured by WebSocketTokenHandshakeInterceptor while the
//    handshake request was still available. The remaining branches are defensive fallbacks.
    private String resolveToken(WebSocketSession session) {
        Object handshakeToken = session.getAttributes()
                .get(WebSocketTokenHandshakeInterceptor.HANDSHAKE_TOKEN_ATTRIBUTE);
        if (handshakeToken instanceof String token && !token.isBlank()) {
            return token;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getDetails() instanceof String detailsToken && !detailsToken.isBlank()) {
                return detailsToken;
            }
            if (authentication.getCredentials() instanceof String credentialsToken && !credentialsToken.isBlank()) {
                return credentialsToken;
            }
        }
        return UriComponentsBuilder.fromUri(session.getUri())
                .build()
                .getQueryParams()
                .getFirst("token");
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
                    log.info("New message from user: {}", Optional.ofNullable(session.getPrincipal()).map(Principal::getName).orElse(""));

                    String[] test = fullMessage.split(Message.MESSAGE_ENDER);

                    Message protocolMessage = websocketMessageDecryptor.decipher(test[test.length - 1]);

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
                Arrays.stream(iE.getStackTrace()).forEach(stackTraceElement -> {
                    log.error(stackTraceElement.toString());
                });
                connectionBroker.handleException(session, iE.getCause());
            } else {
                log.error(e);
                connectionBroker.handleException(session, e);
            }
            partialMessagesMap.remove(session.getId());
        } finally {
            log.info("End of message processing - session: {} from: {}", session.getId(), Optional.ofNullable(session.getPrincipal()).map(Principal::getName).orElse(""));
            webSocketDataHolder.clearCurrentSession();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Error {} - session: {} from: {}", exception.getMessage(), session.getId(), Optional.ofNullable(session.getPrincipal()).map(Principal::getName).orElse(""));
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
        return true;
    }

    public void registerTopic(String... topic) {
        webSocketDataHolder.registerTopic(topic);
    }
}
