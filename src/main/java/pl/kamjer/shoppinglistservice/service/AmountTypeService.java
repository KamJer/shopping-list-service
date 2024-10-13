package pl.kamjer.shoppinglistservice.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AddDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.Dto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.LocalDateTimeDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class AmountTypeService extends CustomService{

    private final AmountTypeRepository amountTypeRepository;

    public AmountTypeService(AmountTypeRepository amountTypeRepository, UserRepository userRepository) {
        super(userRepository);
        this.amountTypeRepository = amountTypeRepository;
    }


    @Transactional
    public AddDto insertAmountType(AmountTypeDto amountTypeDto) throws NoResourcesFoundException {
        LocalDateTime savedTime = LocalDateTime.now();
        User user = updateSaveTimeInUser(savedTime);
        return AddDto.builder()
                .newId(amountTypeRepository.save(DatabaseUtil.toAmountType(user, amountTypeDto, savedTime)).getAmountTypeId().getAmountTypeId())
                .savedTime(savedTime)
                .build();
    }
    @Transactional
    public LocalDateTimeDto updateAmountType(AmountTypeDto amountTypeDto) throws NoResourcesFoundException {
        LocalDateTime savedTime = LocalDateTime.now();
        User user = updateSaveTimeInUser(savedTime);
        amountTypeRepository.save(DatabaseUtil.toAmountType(user, amountTypeDto, savedTime));
        return LocalDateTimeDto.builder().savedTime(savedTime).build();
    }

    @Transactional
    public LocalDateTimeDto deleteAmountType(Long amountTypeId) throws NoResourcesFoundException {
        LocalDateTime savedTime = LocalDateTime.now();
        User user = updateSaveTimeInUser(savedTime);
        AmountType amountTypeToDelete = amountTypeRepository
                .findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(user.getUserName(), amountTypeId)
                .orElseThrow(() -> new NoResourcesFoundException("No such AmountType found: " + user.getUserName() + ", " + amountTypeId));
        amountTypeToDelete.setDeleted(true);
        amountTypeRepository.save(amountTypeToDelete);
        return LocalDateTimeDto.builder().savedTime(savedTime).build();
    }

    @Transactional
    public Dto synchronizeAmountTypeDto(AmountTypeDto amountTypeDto) {
        return switch (amountTypeDto.getModifyState()) {
            case INSERT -> insertAmountType(amountTypeDto);
            case UPDATE -> updateAmountType(amountTypeDto);
            case DELETE -> deleteAmountType(amountTypeDto.getAmountTypeId());
            case NONE -> null;
        };
    }
}
