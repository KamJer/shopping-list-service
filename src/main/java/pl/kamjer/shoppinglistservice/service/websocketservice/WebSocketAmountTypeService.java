package pl.kamjer.shoppinglistservice.service.websocketservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.mapping.ShoppingEntityMapper;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WebSocketAmountTypeService extends WebsocketCustomService {

    private final AmountTypeRepository amountTypeRepository;
    private final ShoppingEntityMapper shoppingEntityMapper;

    public WebSocketAmountTypeService(SecClient secClient, WebSocketDataHolder webSocketDataHolder,
                                      AmountTypeRepository amountTypeRepository, ObjectMapper objectMapper,
                                      ShoppingEntityMapper shoppingEntityMapper) {
        super(webSocketDataHolder, secClient, objectMapper);
        this.amountTypeRepository = amountTypeRepository;
        this.shoppingEntityMapper = shoppingEntityMapper;
    }

    @Transactional
    public AmountTypeDto putAmountType(AmountTypeDto amountTypeDto) {
        User user = requireAuthenticatedUser();
        LocalDateTime savedTime = LocalDateTime.now();
        AmountType amountTypeToPut = shoppingEntityMapper.toAmountType(user, amountTypeDto, savedTime);
        amountTypeRepository.save(amountTypeToPut);
        amountTypeToPut.setLocalId(amountTypeDto.getLocalId());
        amountTypeToPut.setSavedTime(savedTime);
        user.setSavedTime(savedTime);
        secClient.putUser(shoppingEntityMapper.toUserDto(user), user.getPassword());
        return shoppingEntityMapper.toAmountTypeDto(amountTypeToPut, ModifyState.UPDATE, savedTime);
    }

    @Transactional
    public AmountTypeDto postAmountType(AmountTypeDto amountTypeDto) {
        User user = requireAuthenticatedUser();
        LocalDateTime savedTime = LocalDateTime.now();
        Optional<AmountType> amountTypeOptional = amountTypeRepository
                .findAmountTypeByUserNameAndAmountTypeId(user.getUserName(), amountTypeDto.getAmountTypeId());
        if (amountTypeOptional.isPresent()) {
            AmountType amountType = amountTypeOptional.get();
            amountType.setTypeName(amountTypeDto.getTypeName());
            amountType.setDeleted(amountTypeDto.isDeleted());
            amountType.setLocalId(amountTypeDto.getLocalId());
            amountType.setSavedTime(savedTime);
            user.setSavedTime(savedTime);
            secClient.putUser(shoppingEntityMapper.toUserDto(user), user.getPassword());
            return shoppingEntityMapper.toAmountTypeDto(amountType, ModifyState.UPDATE, savedTime);
        }
//        if updated amountType does not exist insert in to the database
        return putAmountType(amountTypeDto);
    }

    @Transactional
    public AmountTypeDto deleteAmountType(AmountTypeDto amountTypeDto) {
        User user = requireAuthenticatedUser();
        LocalDateTime savedTime = LocalDateTime.now();
        Optional<AmountType> amountTypeOptional = amountTypeRepository
                .findAmountTypeByUserNameAndAmountTypeId(user.getUserName(), amountTypeDto.getAmountTypeId());
        if (amountTypeOptional.isPresent()) {
            AmountType amountTypeToDelete = amountTypeOptional.get();
            amountTypeToDelete.setDeleted(amountTypeDto.isDeleted());
            amountTypeToDelete.setLocalId(amountTypeDto.getLocalId());
            amountTypeToDelete.setSavedTime(savedTime);
            amountTypeToDelete.getShoppingItemList().forEach(shoppingItem -> shoppingItem.setDeleted(true));
            user.setSavedTime(savedTime);
            secClient.putUser(shoppingEntityMapper.toUserDto(user), user.getPassword());
            return shoppingEntityMapper.toAmountTypeDto(amountTypeToDelete, ModifyState.DELETE, savedTime);
        }
//        if data does not exist in a database send it to client to delete anyway since it does not exist no action necessary
        return shoppingEntityMapper.copyAmountTypeDto(amountTypeDto, ModifyState.DELETE);
    }
}
