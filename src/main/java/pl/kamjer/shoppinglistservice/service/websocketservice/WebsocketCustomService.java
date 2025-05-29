package pl.kamjer.shoppinglistservice.service.websocketservice;

import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.repository.UserRepository;
import pl.kamjer.shoppinglistservice.service.CustomService;

import java.util.Optional;

public class WebsocketCustomService extends CustomService {

    private final WebSocketDataHolder webSocketDataHolder;

    public WebsocketCustomService(UserRepository userRepository, WebSocketDataHolder webSocketDataHolder) {
        super(userRepository);
        this.webSocketDataHolder = webSocketDataHolder;
    }

    @Override
    public User getUserFromAuth() throws NoResourcesFoundException {
        String userName = Optional.ofNullable(webSocketDataHolder.getCurrentSession().getPrincipal()).orElseThrow().getName();
        return userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No such User"));
    }
}
