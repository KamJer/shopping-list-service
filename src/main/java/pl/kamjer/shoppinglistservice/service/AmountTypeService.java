package pl.kamjer.shoppinglistservice.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.dialect.Database;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.AmountTypeId;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AddDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AmountTypeService {

    private AmountTypeRepository amountTypeRepository;
    private UserRepository userRepository;

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
    public LocalDateTime updateAmountType(AmountTypeDto amountTypeDto) throws NoResourcesFoundException {
        LocalDateTime savedTime = LocalDateTime.now();
        User user = updateSaveTimeInUser(savedTime);
        amountTypeRepository.save(DatabaseUtil.toAmountType(user, amountTypeDto, savedTime));
        return savedTime;
    }

    @Transactional
    public LocalDateTime deleteAmountType(Long amountTypeId) throws NoResourcesFoundException {
        LocalDateTime savedTime = LocalDateTime.now();
        User user = updateSaveTimeInUser(savedTime);
        AmountType amountTypeToDelete = amountTypeRepository
                .findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(user.getUserName(), amountTypeId)
                .orElseThrow(() -> new NoResourcesFoundException("No such AmountType found: " + user.getUserName() + ", " + amountTypeId));
        amountTypeToDelete.setDeleted(true);
        amountTypeRepository.save(amountTypeToDelete);
        return savedTime;
    }

    private User updateSaveTimeInUser(LocalDateTime localDateTime) throws NoResourcesFoundException {
        User user = getUserFromAuth();
        user.setSavedTime(localDateTime);
        userRepository.save(user);
        return user;
    }

    private User getUserFromAuth() throws NoResourcesFoundException {
        String userName = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No such User"));
    }
}
