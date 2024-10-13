package pl.kamjer.shoppinglistservice.service.websocketservice;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.ShoppingItemRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;
import pl.kamjer.shoppinglistservice.service.UtilService;

import java.util.Optional;

@Service
public class WebSocketService extends UtilService {

    public WebSocketDataHolder webSocketDataHolder;

    public WebSocketService(UserRepository userRepository,
                            AmountTypeRepository amountTypeRepository,
                            CategoryRepository categoryRepository,
                            ShoppingItemRepository shoppingItemRepository,
                            WebSocketDataHolder webSocketDataHolder) {
        super(userRepository,
                amountTypeRepository,
                categoryRepository,
                shoppingItemRepository);
        this.webSocketDataHolder = webSocketDataHolder;
    }


    @Override
    public User getUserFromAuth() throws NoResourcesFoundException {
        String userName = Optional.ofNullable(webSocketDataHolder.getCurrentSession().getPrincipal()).orElseThrow().getName();
        return userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No such User"));
    }

    @Transactional
    public AllDto synchronizeWebSocket(AllDto allDto) {
        return synchronizeDto(allDto);
    }
}
