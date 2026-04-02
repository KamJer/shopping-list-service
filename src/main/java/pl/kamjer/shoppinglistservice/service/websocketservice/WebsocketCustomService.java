package pl.kamjer.shoppinglistservice.service.websocketservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.service.CustomService;

import java.security.Principal;
import java.util.Optional;

@Service
public class WebsocketCustomService extends CustomService {

    private final WebSocketDataHolder webSocketDataHolder;

    public WebsocketCustomService(WebSocketDataHolder webSocketDataHolder, SecClient secClient, ObjectMapper objectMapper) {
        super(secClient, objectMapper);
        this.webSocketDataHolder = webSocketDataHolder;
    }

    @Override
    public Optional<User> getUserFromAuth() {
        WebSocketSession session = webSocketDataHolder.getCurrentSession();
        if (session == null) {
            return Optional.empty();
        }
        Optional<String> userName = Optional.ofNullable(session.getPrincipal()).map(Principal::getName);
        if (userName.isEmpty()) {
            return Optional.empty();
        }
        Object tokenObj = session.getAttributes().get("TOKEN");
        if (tokenObj == null) {
            return Optional.empty();
        }
        String token = tokenObj.toString();
        try {
            User user = objectMapper.convertValue(secClient.getUserByUserName(userName.get(), token), User.class);
            if (user == null) {
                return Optional.empty();
            }
            user.setPassword(token);
            return Optional.of(user);
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    protected User requireAuthenticatedUser() {
        return getUserFromAuth().orElseThrow(() -> new NoResourcesFoundException("User not authenticated"));
    }
}
