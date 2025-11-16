package pl.kamjer.shoppinglistservice.service.websocketservice;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WebSocketAmountTypeService extends WebsocketCustomService {

    private final AmountTypeRepository amountTypeRepository;

    public WebSocketAmountTypeService(SecClient secClient, WebSocketDataHolder webSocketDataHolder, AmountTypeRepository amountTypeRepository) {
        super(webSocketDataHolder, secClient);
        this.amountTypeRepository = amountTypeRepository;
    }

    @Transactional
    public AmountTypeDto putAmountType(AmountTypeDto amountTypeDto) {
        LocalDateTime savedTime = LocalDateTime.now();
        AmountType amountTypeToPut = DatabaseUtil.toAmountType(getUserFromAuth(), amountTypeDto, LocalDateTime.now());
        amountTypeRepository.save(amountTypeToPut);
        amountTypeToPut.setLocalId(amountTypeDto.getLocalId());
        amountTypeToPut.setSavedTime(savedTime);
        return DatabaseUtil.toAmountTypeDto(amountTypeToPut, ModifyState.UPDATE, savedTime);
    }

    @Transactional
    public AmountTypeDto postAmountType(AmountTypeDto amountTypeDto) {
        LocalDateTime savedTime = LocalDateTime.now();
        Optional<AmountType> amountTypeOptional = amountTypeRepository.findAmountTypeByUserNameAndAmountTypeId(getUserFromAuth().getUserName(), amountTypeDto.getAmountTypeId());
        if (amountTypeOptional.isPresent()) {
            AmountType amountType = amountTypeOptional.get();
            amountType.setTypeName(amountTypeDto.getTypeName());
            amountType.setDeleted(amountTypeDto.isDeleted());
            amountType.setSavedTime(LocalDateTime.now());
            amountType.setLocalId(amountTypeDto.getLocalId());
            amountType.setSavedTime(savedTime);
            return DatabaseUtil.toAmountTypeDto(amountType, ModifyState.UPDATE, savedTime);
        }
//        if updated amountType does not exist insert in to the database
        return postAmountType(amountTypeDto);
    }

    @Transactional
    public AmountTypeDto deleteAmountType(AmountTypeDto amountTypeDto) {
        LocalDateTime savedTime = LocalDateTime.now();
        Optional<AmountType> amountTypeOptional = amountTypeRepository.findAmountTypeByUserNameAndAmountTypeId(getUserFromAuth().getUserName(), amountTypeDto.getAmountTypeId());
        if (amountTypeOptional.isPresent()) {
            AmountType amountTypeToDelete = amountTypeOptional.get();
            amountTypeToDelete.setDeleted(amountTypeDto.isDeleted());
            amountTypeToDelete.setLocalId(amountTypeDto.getLocalId());
            amountTypeToDelete.setSavedTime(savedTime);
            amountTypeToDelete.getShoppingItemList().forEach(shoppingItem -> shoppingItem.setDeleted(true));
            return DatabaseUtil.toAmountTypeDto(amountTypeToDelete, ModifyState.DELETE, savedTime);
        }
//        if data does not exist in a database send it to client to delete anyway since it does not exist no action necessary
        return DatabaseUtil.amountTypeDtoToAmountTypeDto(amountTypeDto, ModifyState.DELETE);
    }
}
