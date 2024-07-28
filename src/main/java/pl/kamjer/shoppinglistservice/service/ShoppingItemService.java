package pl.kamjer.shoppinglistservice.service;

import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.ShoppingItem;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.ShoppingItemRepository;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ShoppingItemService {

    private ShoppingItemRepository shoppingItemRepository;
    private UserRepository userRepository;
    private AmountTypeRepository amountTypeRepository;
    private CategoryRepository categoryRepository;

    @PreAuthorize("#shoppingItemDto.userName == authentication.principal.username")
    public Long insertShoppingItem(ShoppingItemDto shoppingItemDto) throws NoResourcesFoundException {
        return shoppingItemRepository.save(DatabaseUtil.toShoppingItem(userRepository,
                        amountTypeRepository,
                        categoryRepository,
                        shoppingItemDto))
                .getShoppingItemId()
                .getShoppingItemId();
    }

    @PreAuthorize("#shoppingItemDto.userName == authentication.principal.username")
    public void updateShoppingItem(ShoppingItemDto shoppingItemDto) throws NoResourcesFoundException {
        shoppingItemRepository.save(DatabaseUtil.toShoppingItem(userRepository, amountTypeRepository, categoryRepository, shoppingItemDto));
    }

    @PreAuthorize("#userName == authentication.principal.username")
    public List<ShoppingItemDto> getShoppingItemsByUser(String userName) throws NoResourcesFoundException {
        userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No Such User"));
        return shoppingItemRepository.findAllByShoppingItemIdUserUserName(userName).stream().map(DatabaseUtil::toShoppingItemDto).collect(Collectors.toList());
    }

    @PreAuthorize("#shoppingItemDto.userName == authentication.principal.username")
    public void deleteShoppingItem(ShoppingItemDto shoppingItemDto) throws NoResourcesFoundException {
        shoppingItemRepository.delete(DatabaseUtil.toShoppingItem(userRepository, amountTypeRepository, categoryRepository, shoppingItemDto));
    }
}
