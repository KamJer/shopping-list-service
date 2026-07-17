package pl.kamjer.shoppinglistservice.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketHandlerTest {

    @Mock
    private MessageValidator messageValidator;
    @Mock
    private WebSocketDataHolder webSocketDataHolder;
    @Mock
    private ConnectionBroker connectionBroker;
    @Mock
    private WebsocketMessageDecryptor websocketMessageDecryptor;
    @Mock
    private WebSocketSession session;

    private WebSocketHandler handler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        handler = new WebSocketHandler(messageValidator, webSocketDataHolder, connectionBroker, websocketMessageDecryptor);
    }

    private Message buildMessage(Message.Command command, HashMap<Message.Header, String> headers) {
        return new Message(command, headers);
    }

    private TextMessage wrapAsTextMessage(Message message) throws Exception {
        String json = objectMapper.writeValueAsString(message);
        return new TextMessage(json + "\0");
    }

    @Test
    void afterConnectionEstablished_registersSession() throws Exception {
        ConcurrentHashMap<String, Object> attrs = new ConcurrentHashMap<>();
        when(session.getId()).thenReturn("sess-1");
        when(session.getUri()).thenReturn(new URI("ws://localhost:8080/ws?token=abc123"));
        when(session.getPrincipal()).thenReturn(null);
        when(session.getAttributes()).thenReturn(attrs);

        handler.afterConnectionEstablished(session);

        verify(webSocketDataHolder).putSessionConnected(session);
        assertThat(attrs.get("TOKEN")).isEqualTo("abc123");
    }

    @Test
    void handleMessage_connectCommand_delegatesToConnectionBroker() throws Exception {
        HashMap<Message.Header, String> headers = new HashMap<>();
        Message message = buildMessage(Message.Command.CONNECT, headers);
        TextMessage textMessage = wrapAsTextMessage(message);

        when(session.getId()).thenReturn("sess-1");
        when(session.getPrincipal()).thenReturn(null);
        when(messageValidator.validateMessage(any())).thenReturn(true);
        when(websocketMessageDecryptor.decipher(anyString())).thenReturn(message);

        handler.handleMessage(session, textMessage);

        verify(connectionBroker).handleConnect(session);
    }

    @Test
    void handleMessage_messageCommand_delegatesToConnectionBroker() throws Exception {
        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.ID, "sess-1");
        headers.put(Message.Header.DEST, "/synchronizeData");
        headers.put(Message.Header.BODY, "{}");
        Message message = buildMessage(Message.Command.MESSAGE, headers);
        TextMessage textMessage = wrapAsTextMessage(message);

        when(session.getId()).thenReturn("sess-1");
        when(session.getPrincipal()).thenReturn(null);
        when(messageValidator.validateMessage(any())).thenReturn(true);
        when(websocketMessageDecryptor.decipher(anyString())).thenReturn(message);

        handler.handleMessage(session, textMessage);

        verify(connectionBroker).handleMessage(session, message);
    }

    @Test
    void handleMessage_subscribeCommand_delegatesToConnectionBroker() throws Exception {
        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.DEST, "/synchronizeData");
        Message message = buildMessage(Message.Command.SUBSCRIBE, headers);
        TextMessage textMessage = wrapAsTextMessage(message);

        when(session.getId()).thenReturn("sess-1");
        when(session.getPrincipal()).thenReturn(null);
        when(messageValidator.validateMessage(any())).thenReturn(true);
        when(websocketMessageDecryptor.decipher(anyString())).thenReturn(message);

        handler.handleMessage(session, textMessage);

        verify(connectionBroker).handleSubscribe(session, message);
    }

    @Test
    void handleMessage_unsubscribeCommand_delegatesToConnectionBroker() throws Exception {
        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.DEST, "/synchronizeData");
        Message message = buildMessage(Message.Command.UNSUBSCRIBE, headers);
        TextMessage textMessage = wrapAsTextMessage(message);

        when(session.getId()).thenReturn("sess-1");
        when(session.getPrincipal()).thenReturn(null);
        when(messageValidator.validateMessage(any())).thenReturn(true);
        when(websocketMessageDecryptor.decipher(anyString())).thenReturn(message);

        handler.handleMessage(session, textMessage);

        verify(connectionBroker).handleUnsubscribe(session, message);
    }

    @Test
    void handleMessage_invalidValidation_throwsAndHandlesException() throws Exception {
        HashMap<Message.Header, String> headers = new HashMap<>();
        Message message = buildMessage(Message.Command.MESSAGE, headers);
        TextMessage textMessage = wrapAsTextMessage(message);

        when(session.getId()).thenReturn("sess-1");
        when(session.getPrincipal()).thenReturn(null);
        when(messageValidator.validateMessage(any())).thenReturn(false);
        when(websocketMessageDecryptor.decipher(anyString())).thenReturn(message);

        handler.handleMessage(session, textMessage);

        verify(connectionBroker).handleException(eq(session), any(IllegalArgumentException.class));
    }

    @Test
    void handleMessage_setsAndClearsCurrentSession() throws Exception {
        HashMap<Message.Header, String> headers = new HashMap<>();
        Message message = buildMessage(Message.Command.CONNECT, headers);
        TextMessage textMessage = wrapAsTextMessage(message);

        when(session.getId()).thenReturn("sess-1");
        when(session.getPrincipal()).thenReturn(null);
        when(messageValidator.validateMessage(any())).thenReturn(true);
        when(websocketMessageDecryptor.decipher(anyString())).thenReturn(message);

        handler.handleMessage(session, textMessage);

        verify(webSocketDataHolder).setCurrentSession(session);
        verify(webSocketDataHolder).clearCurrentSession();
    }

    @Test
    void handleTransportError_removesSessionFromTopics() throws Exception {
        when(session.getId()).thenReturn("sess-1");
        when(session.getPrincipal()).thenReturn(null);

        handler.handleTransportError(session, new RuntimeException("transport error"));

        verify(webSocketDataHolder).removeSessionFromTopics(session);
        verify(connectionBroker).handleException(eq(session), any(RuntimeException.class));
    }

    @Test
    void afterConnectionClosed_removesSessionFromTopics() throws Exception {
        when(session.getId()).thenReturn("sess-1");

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        verify(webSocketDataHolder).removeSessionFromTopics(session);
    }

    @Test
    void supportsPartialMessages_returnsTrue() {
        assertThat(handler.supportsPartialMessages()).isTrue();
    }

    @Test
    void registerTopic_delegatesToDataHolder() {
        handler.registerTopic("/synchronizeData", "/{userName}/pip");

        verify(webSocketDataHolder).registerTopic("/synchronizeData", "/{userName}/pip");
    }
}
