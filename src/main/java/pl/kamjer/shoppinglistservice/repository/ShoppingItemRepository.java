package pl.kamjer.shoppinglistservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.ShoppingItem;
import pl.kamjer.shoppinglistservice.model.ShoppingItemId;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Flow;

@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, ShoppingItemId> {
    Optional<ShoppingItem> findByShoppingItemIdUserUserNameAndShoppingItemIdShoppingItemId(String userName, Long shoppingItemId);
    List<ShoppingItem> findShoppingItemByShoppingItemIdUserUserNameAndSavedTimeAfter(String userName, LocalDateTime savedTime);
    List<ShoppingItem> findShoppingItemByShoppingItemIdUserUserNameAndItemCategory(String shoppingItemId_user_userName, Category itemCategory);

    List<ShoppingItem> findBySavedTimeBeforeAndBoughtIsTrue(LocalDateTime deleteTime);
}