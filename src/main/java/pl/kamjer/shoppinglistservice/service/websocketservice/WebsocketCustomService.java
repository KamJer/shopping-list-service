package pl.kamjer.shoppinglistservice.service.websocketservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.service.CustomService;

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
        String userName = Optional.ofNullable(webSocketDataHolder.getCurrentSession().getPrincipal()).orElseThrow().getName();
        return objectMapper.convertValue(secClient.getUserByUserName(userName), User.class);
    }
}
