package pl.kamjer.shoppinglistservice.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.*;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.ShoppingItemRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CustomService {

    protected UserRepository userRepository;

    public User getUserFromAuth() throws NoResourcesFoundException {
        String userName = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No such User"));
    }
}
