package pl.kamjer.shoppinglistservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamjer.shoppinglistservice.model.ShoppingItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
    Optional<ShoppingItem> findShoppingItemByUserNameAndShoppingItemId(String userName, Long shoppingItemId);
    List<ShoppingItem> findShoppingItemByUserNameAndSavedTimeAfter(String userName, LocalDateTime savedTime);
    List<ShoppingItem> findBySavedTimeBeforeAndBoughtIsTrue(LocalDateTime deleteTime);
    List<ShoppingItem> findByUserName(String userName);
}