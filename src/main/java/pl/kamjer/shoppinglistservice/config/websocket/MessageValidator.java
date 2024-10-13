package pl.kamjer.shoppinglistservice.config.websocket;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.util.ArrayUtils;

import java.util.List;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains;
import static pl.kamjer.shoppinglistservice.config.websocket.Message.Header.*;
@Component
public class MessageValidator {

    private static final List<Message.Header> CONNECT_REQUIRED_HEADER = ArrayUtils.toUnmodifiableList(new Message.Header[] {});
    private static final List<Message.Header> CONNECTED_REQUIRED_HEADER = ArrayUtils.toUnmodifiableList(new Message.Header[] {ID});
    private static final List<Message.Header> SUBSCRIBE_REQUIRED_HEADER = ArrayUtils.toUnmodifiableList(new Message.Header[] {DEST});
    private static final List<Message.Header> SUBSCRIBED_REQUIRED_HEADER = ArrayUtils.toUnmodifiableList(new Message.Header[] {});
    private static final List<Message.Header> UNSUBSCRIBE_REQUIRED_HEADER = ArrayUtils.toUnmodifiableList(new Message.Header[] {DEST});
    private static final List<Message.Header> UNSUBSCRIBED_REQUIRED_HEADER = ArrayUtils.toUnmodifiableList(new Message.Header[] {});
    private static final List<Message.Header> MESSAGE_REQUIRED_HEADER = ArrayUtils.toUnmodifiableList(new Message.Header[] {ID, DEST, BODY});
    private static final List<Message.Header> ERROR_REQUIRED_HEADER = ArrayUtils.toUnmodifiableList(new Message.Header[] {BODY});
    public boolean validateMessage(Message message) {
        final boolean[] correct = {true};
        switch (message.getCommand()) {
            case CONNECT -> CONNECT_REQUIRED_HEADER.forEach(header -> correct[0] = correct[0] && message.getHeaders().containsKey(header));
            case CONNECTED -> CONNECTED_REQUIRED_HEADER.forEach(header ->  correct[0] = correct[0] && message.getHeaders().containsKey(header));
            case SUBSCRIBE -> SUBSCRIBE_REQUIRED_HEADER.forEach(header ->  correct[0] = correct[0] && message.getHeaders().containsKey(header));
            case SUBSCRIBED -> SUBSCRIBED_REQUIRED_HEADER.forEach(header ->  correct[0] = correct[0] && message.getHeaders().containsKey(header));
            case UNSUBSCRIBE -> UNSUBSCRIBE_REQUIRED_HEADER.forEach(header ->  correct[0] = correct[0] && message.getHeaders().containsKey(header));
            case UNSUBSCRIBED -> UNSUBSCRIBED_REQUIRED_HEADER.forEach(header ->  correct[0] = correct[0] && message.getHeaders().containsKey(header));
            case MESSAGE -> MESSAGE_REQUIRED_HEADER.forEach(header ->  correct[0] = correct[0] && message.getHeaders().containsKey(header));
            case ERROR -> ERROR_REQUIRED_HEADER.forEach(header ->  correct[0] = correct[0] && message.getHeaders().containsKey(header));
        }
        return correct[0];
    }
}
