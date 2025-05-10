package pl.kamjer.shoppinglistservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.ShoppingItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
    Optional<ShoppingItem> findShoppingItemByUserUserNameAndShoppingItemId(String userName, Long shoppingItemId);
    List<ShoppingItem> findShoppingItemByUserUserNameAndSavedTimeAfter(String userName, LocalDateTime savedTime);
    List<ShoppingItem> findShoppingItemByUserUserNameAndItemCategory(String user_userName, Category itemCategory);
    List<ShoppingItem> findShoppingItemByUserUserNameAndItemAmountType(String user_userName, AmountType itemAmountType);

    List<ShoppingItem> findBySavedTimeBeforeAndBoughtIsTrue(LocalDateTime deleteTime);
    List<ShoppingItem> findByUserUserName(String userName);
}