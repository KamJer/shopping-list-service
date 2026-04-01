package pl.kamjer.shoppinglistservice.config.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;
import pl.kamjer.shoppinglistservice.model.User;

@Getter
@AllArgsConstructor
public class UserSession {
    private WebSocketSession webSocketSession;
    private User user;
}
