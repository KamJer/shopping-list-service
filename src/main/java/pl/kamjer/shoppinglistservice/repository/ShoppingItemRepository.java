package pl.kamjer.shoppinglistservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Bulk UPDATE omija persistence context Hibernate. flushAutomatically wymusza flush przed query
    // (nie gubi wcześniejszych zmian), clearAutomatically czyści context po query (eliminuje ryzyko
    // starych danych w załadowanych encjach). Bez tych flag encje w pamięci mogą mieć nieaktualne wartości.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ShoppingItem si SET si.deleted = true WHERE si.itemAmountType.amountTypeId = :amountTypeId")
    void markAllDeletedByAmountTypeId(@Param("amountTypeId") Long amountTypeId);

    // j.w.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ShoppingItem si SET si.deleted = true WHERE si.itemCategory.categoryId = :categoryId")
    void markAllDeletedByCategoryId(@Param("categoryId") Long categoryId);
}