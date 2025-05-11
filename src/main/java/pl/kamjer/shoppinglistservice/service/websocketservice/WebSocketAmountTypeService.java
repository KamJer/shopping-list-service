package pl.kamjer.shoppinglistservice.service.websocketservice;

import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;
import pl.kamjer.shoppinglistservice.service.CustomService;

import java.time.LocalDateTime;

@Service
public class WebSocketAmountTypeService extends CustomService {

    private AmountTypeRepository amountTypeRepository;

    public WebSocketAmountTypeService(UserRepository userRepository) {
        super(userRepository);
    }

    public AmountTypeDto putAmountType(AmountTypeDto amountTypeDto) {
        AmountType amountTypeToPut = DatabaseUtil.toAmountType(getUserFromAuth(), amountTypeDto, LocalDateTime.now());
        amountTypeRepository.save(amountTypeToPut);
        return DatabaseUtil.toAmountTypeDto(amountTypeToPut, ModifyState.UPDATE);
    }
}
