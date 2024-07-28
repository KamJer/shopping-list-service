package pl.kamjer.shoppinglistservice.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.AmountTypeId;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AmountTypeService {

    private AmountTypeRepository amountTypeRepository;
    private UserRepository userRepository;

    @PreAuthorize("#amountTypeDto.userName == authentication.principal.username")
    public Long insertAmountType(AmountTypeDto amountTypeDto) throws NoResourcesFoundException {
        return amountTypeRepository.save(DatabaseUtil.toAmountType(userRepository, amountTypeDto)).getAmountTypeId().getAmountTypeId();
    }

    @PreAuthorize("#amountTypeDto.userName == authentication.principal.username")
    public void updateAmountType(AmountTypeDto amountTypeDto) throws NoResourcesFoundException {
        DatabaseUtil.toAmountTypeDto(amountTypeRepository.save(DatabaseUtil.toAmountType(userRepository, amountTypeDto)));
    }

    @PreAuthorize("#userName == authentication.principal.username")
    public List<AmountTypeDto> getAmountTypeByUser(String userName) throws NoResourcesFoundException {
        userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No such User found"));
        return amountTypeRepository.findAmountTypeByAmountTypeIdUserUserName(userName).stream().map(DatabaseUtil::toAmountTypeDto).collect(Collectors.toList());
    }

    @PreAuthorize("#amountTypeDto.userName == authentication.principal.username")
    public void deleteAmountType(AmountTypeDto amountTypeDto) throws NoResourcesFoundException {
        amountTypeRepository.delete(DatabaseUtil.toAmountType(userRepository, amountTypeDto));
    }


}
