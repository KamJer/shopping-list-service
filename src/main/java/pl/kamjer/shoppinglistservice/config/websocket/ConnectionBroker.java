package pl.kamjer.shoppinglistservice.config.websocket;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class ConnectionBroker {

    private BeanInspector beanInspector;
    private WebSocketDataHolder webSocketDataHolder;
    private WebsocketMessageDecryptor websocketMessageDecryptor;

    public void handleConnect(WebSocketSession session) throws IOException {
        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.ID, session.getId());
        Message messageConnectedBack = new Message(Message.Command.CONNECTED, headers);

        session.sendMessage(new TextMessage(websocketMessageDecryptor.jsonphyMessage(messageConnectedBack)));
    }

    public void handleMessage(WebSocketSession session, Message protocolMessage) throws IOException, InvocationTargetException, IllegalAccessException {
        String[] body = protocolMessage.getHeaders().get(Message.Header.BODY).split(";");
        String dest = protocolMessage.getHeaders().get(Message.Header.DEST);
        Optional<String> processedBody = beanInspector.findControllerMethodAndCall(dest, body);

        if (processedBody.isPresent()) {
            HashMap<Message.Header, String> headersSource = new HashMap<>();
            headersSource.put(Message.Header.ID, session.getId());
            headersSource.put(Message.Header.DEST, protocolMessage.getHeaders().get(Message.Header.DEST));
            headersSource.put(Message.Header.BODY, processedBody.get());
            Message messageBack = new Message(Message.Command.MESSAGE, headersSource);

            session.sendMessage(new TextMessage(websocketMessageDecryptor.jsonphyMessage(messageBack)));

            List<WebSocketSession> sessions =  webSocketDataHolder.getSessionsForTopic(protocolMessage.getHeaders().get(Message.Header.DEST));
            for (WebSocketSession webSocketSession : sessions) {
                if (!webSocketSession.equals(session)) {
                    webSocketSession.sendMessage(new TextMessage(websocketMessageDecryptor.jsonphyMessage(messageBack)));
                }
            }
        }
    }

    public void handleSubscribe(WebSocketSession session, Message protocolMessage) throws IOException {
        String dest = protocolMessage.getHeaders().get(Message.Header.DEST);
        HashMap<Message.Header, String> headersSubscribed = new HashMap<>();

        if (protocolMessage.getHeaders().get(Message.Header.PARA) != null) {
            String[] parameters = Arrays.stream(protocolMessage.getHeaders().get(Message.Header.PARA).split(";"))
                    .filter(s -> !s.isEmpty()).toArray(String[]::new);

//            checking of passed parameters and parameters in url match
            if (parameters.length != StringUtils.countOccurrencesOf(dest, "{")) {
                throw new IllegalArgumentException("Wrong amount of parameters");
            }

//            splitting url on elements
            StringBuilder subUrlBuilder = getSubUrlBuilder(dest, parameters);
            headersSubscribed.put(Message.Header.DEST, subUrlBuilder.toString());
            webSocketDataHolder.menageParameterTopics(new Topic(dest, parameters.length), subUrlBuilder.toString(), session);
            webSocketDataHolder.addSessionToTopic(subUrlBuilder.toString(), session);
        } else {
            headersSubscribed.put(Message.Header.DEST, dest);
            webSocketDataHolder.addSessionToTopic(dest, session);
        }
        Message subscribedMessage = new Message(Message.Command.SUBSCRIBED, headersSubscribed);
        session.sendMessage(new TextMessage(websocketMessageDecryptor.jsonphyMessage(subscribedMessage)));
    }

    private static StringBuilder getSubUrlBuilder(String dest, String[] parameters) {
        String[] urlElements = dest.split("/");
        int parameterCount = 0;
        StringBuilder subUrlBuilder = new StringBuilder();
//            beginning of a url element
        for (int i = 0; i < urlElements.length; i++) {

//                if element of url is parameter replace it with passed parameter
            if (urlElements[i].startsWith("{") && urlElements[i].endsWith("}")) {
                subUrlBuilder.append(parameters[parameterCount]);
                parameterCount ++;
            } else {
//                    if element is not parameter add it back at its place
                subUrlBuilder.append(urlElements[i]);
            }
            if (i < urlElements.length - 1) {
//                end of a url element with the exception of a last one
                subUrlBuilder.append("/");
            }
        }
        return subUrlBuilder;
    }

    public void handleUnsubscribe(WebSocketSession session, Message protocolMessage) throws IOException {
        String dest = protocolMessage.getHeaders().get(Message.Header.DEST);
        webSocketDataHolder.removeSubscription(dest, session);

        HashMap<Message.Header, String> headersSubscribed = new HashMap<>();
        Message unsubscribedMessage = new Message(Message.Command.UNSUBSCRIBED, headersSubscribed);
        session.sendMessage(new TextMessage(websocketMessageDecryptor.jsonphyMessage(unsubscribedMessage)));
    }

    public void handleException(WebSocketSession session, Throwable t) throws IOException {
        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.BODY, t.getMessage());

        session.sendMessage(new TextMessage(websocketMessageDecryptor.jsonphyMessage(new Message(Message.Command.ERROR, headers))));
    }
}
