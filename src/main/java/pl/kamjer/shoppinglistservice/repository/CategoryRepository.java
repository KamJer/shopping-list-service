package pl.kamjer.shoppinglistservice.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamjer.shoppinglistservice.model.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByUserUserName(String userName);
    Optional<Category> findCategoryByUserUserNameAndCategoryId(String userName, long categoryId_categoryId);
    List<Category> findByUserUserName(String userName);
    @Transactional
    void deleteByCategoryIdAndUserUserName(Long categoryId, String userName);

    List<Category> findCategoryByUserUserNameAndSavedTimeAfter(String userName, LocalDateTime localDateTime);
    List<Category> findCategoryByUserUserNameAndSavedTimeAfterAndDeletedIsFalse(String userName, LocalDateTime localDateTime);

}
