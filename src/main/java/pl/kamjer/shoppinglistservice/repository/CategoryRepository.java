package pl.kamjer.shoppinglistservice.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByCategoryIdUserUserName(String userName);
    Optional<Category> findCategoryByCategoryIdUserUserNameAndCategoryIdCategoryId(String userName, long categoryId_categoryId);
    @Transactional
    void deleteByCategoryIdCategoryIdAndCategoryIdUserUserName(Long categoryId, String userName);

    List<Category> findCategoryByCategoryIdUserUserNameAndSavedTimeAfter(String userName, LocalDateTime localDateTime);
    List<Category> findCategoryByCategoryIdUserUserNameAndSavedTimeAfterAndDeletedIsFalse(String userName, LocalDateTime localDateTime);

}
