package pl.kamjer.shoppinglistservice.service.websocketservice;

import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;
import pl.kamjer.shoppinglistservice.service.CustomService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WebSocketAmountTypeService extends WebsocketCustomService {

    private final AmountTypeRepository amountTypeRepository;

    public WebSocketAmountTypeService(UserRepository userRepository, WebSocketDataHolder webSocketDataHolder, AmountTypeRepository amountTypeRepository) {
        super(userRepository, webSocketDataHolder);
        this.amountTypeRepository = amountTypeRepository;
    }

    public AmountTypeDto putAmountType(AmountTypeDto amountTypeDto) {
        AmountType amountTypeToPut = DatabaseUtil.toAmountType(getUserFromAuth(), amountTypeDto, LocalDateTime.now());
        amountTypeRepository.save(amountTypeToPut);
        return DatabaseUtil.toAmountTypeDto(amountTypeToPut, ModifyState.UPDATE);
    }

    public AmountTypeDto postAmountType(AmountTypeDto amountTypeDto) {
        Optional<AmountType> amountTypeOptional = amountTypeRepository.findAmountTypeByUserUserNameAndAmountTypeId(getUserFromAuth().getUserName(), amountTypeDto.getAmountTypeId());
        if (amountTypeOptional.isPresent()) {
            AmountType amountType = amountTypeOptional.get();
            amountType.setTypeName(amountTypeDto.getTypeName());
            amountType.setDeleted(amountTypeDto.isDeleted());
            amountType.setSavedTime(LocalDateTime.now());
            return DatabaseUtil.toAmountTypeDto(amountType, ModifyState.UPDATE);
        }
//        if updated amountType does not exist insert in to the database
        return postAmountType(amountTypeDto);
    }

    public AmountTypeDto deleteAmountType(AmountTypeDto amountTypeDto) {
        Optional<AmountType> amountTypeOptional = amountTypeRepository.findAmountTypeByUserUserNameAndAmountTypeId(getUserFromAuth().getUserName(), amountTypeDto.getAmountTypeId());
        if (amountTypeOptional.isPresent()) {
            AmountType amountTypeToDelete = amountTypeOptional.get();
            amountTypeToDelete.setDeleted(amountTypeDto.isDeleted());
            return DatabaseUtil.toAmountTypeDto(amountTypeToDelete, ModifyState.DELETE);
        }
//        if data does not exist in a database send it to client to delete anyway since it does not exist no action necessary
        return DatabaseUtil.amountTypeDtoToAmountTypeDto(amountTypeDto, ModifyState.DELETE);
    }
}
