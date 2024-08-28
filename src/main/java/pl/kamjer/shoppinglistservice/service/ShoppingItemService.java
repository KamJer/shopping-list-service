package pl.kamjer.shoppinglistservice.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.ShoppingItem;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AddDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.ShoppingItemRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShoppingItemService extends CustomService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final AmountTypeRepository amountTypeRepository;
    private final CategoryRepository categoryRepository;

    public ShoppingItemService(ShoppingItemRepository shoppingItemRepository, AmountTypeRepository amountTypeRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        super(userRepository);
        this.shoppingItemRepository = shoppingItemRepository;
        this.amountTypeRepository = amountTypeRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public AddDto insertShoppingItem(ShoppingItemDto shoppingItemDto) throws NoResourcesFoundException {
        LocalDateTime savedTime = LocalDateTime.now();
        User user = updateSaveTimeInUser(savedTime);
        return AddDto.builder().newId(shoppingItemRepository.save(DatabaseUtil.toShoppingItem(user,
                                        amountTypeRepository,
                                        categoryRepository,
                                        shoppingItemDto,
                                        savedTime))
                                .getShoppingItemId()
                                .getShoppingItemId())
                .savedTime(savedTime)
                .build();
    }

    @Transactional
    public LocalDateTime updateShoppingItem(ShoppingItemDto shoppingItemDto) throws NoResourcesFoundException {
        LocalDateTime savedTime = LocalDateTime.now();
        User user = updateSaveTimeInUser(savedTime);
        shoppingItemRepository.save(DatabaseUtil.toShoppingItem(user,
                amountTypeRepository,
                categoryRepository,
                shoppingItemDto,
                savedTime));
        return savedTime;
    }
    @Transactional
    public LocalDateTime deleteShoppingItem(Long shoppingItemId) throws NoResourcesFoundException {
        LocalDateTime savedTime = LocalDateTime.now();
        User user = getUserFromAuth();
        updateSaveTimeInUser(savedTime);
        ShoppingItem shoppingItemToDelete = shoppingItemRepository
                .findByShoppingItemIdUserUserNameAndShoppingItemIdShoppingItemId(user.getUserName(),
                        shoppingItemId).orElseThrow(() -> new NoResourcesFoundException("No such sHoppingItem found: " + user.getUserName() + ", " + shoppingItemId));
        shoppingItemToDelete.setDeleted(true);
        shoppingItemRepository.save(shoppingItemToDelete);
        return savedTime;
    }
}
