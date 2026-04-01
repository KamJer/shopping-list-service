package pl.kamjer.shoppinglistservice.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import pl.kamjer.shoppinglistservice.config.security.JwtAuthToken;
import pl.kamjer.shoppinglistservice.config.security.JwtAuthenticationProvider;
import pl.kamjer.shoppinglistservice.functional_interface.UserProvider;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.*;
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
        session.getAttributes().put("TOKEN", UriComponentsBuilder.fromUri(session.getUri())
                .build()
                .getQueryParams()
                .getFirst("token"));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            webSocketDataHolder.setCurrentSession(session);
            if (message.getPayload() instanceof String payload) {
                StringBuilder partialMessage = partialMessagesMap.computeIfAbsent(session.getId(), k -> new StringBuilder());
                if (partialMessage.toString().endsWith(Message.MESSAGE_ENDER)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    partialMessagesMap.put(session.getId(), stringBuilder);
                    partialMessage = stringBuilder;
                }
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

//    private void validateUser(String auth, WebSocketSession session) throws IOException {
//        try {
//            Authentication authentication = new JwtAuthToken(auth);
//            authenticationManager.authenticate(authentication);
//        } catch (BadCredentialsException ex) {
//            session.close(CloseStatus.NOT_ACCEPTABLE.withReason(ex.getMessage()));
//        }
//    }
}
