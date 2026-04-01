package pl.kamjer.shoppinglistservice.service.websocketservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.config.security.JwtAuthToken;
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
    public User getUserFromAuth() throws NoResourcesFoundException {
        String userName = Optional.ofNullable(webSocketDataHolder.getCurrentSession().getPrincipal()).map(Principal::getName).orElseThrow();
        String token = webSocketDataHolder.getCurrentSession().getAttributes().get("TOKEN").toString();
        User user = objectMapper.convertValue(secClient.getUserByUserName(userName, token), User.class);
        user.setPassword(token);
        return user;
    }
}
