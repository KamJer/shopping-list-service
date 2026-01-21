package pl.kamjer.shoppinglistservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamjer.shoppinglistservice.model.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findCategoryByUserNameAndCategoryId(String userName, long categoryId_categoryId);
    List<Category> findByUserName(String userName);
    List<Category> findCategoryByUserNameAndSavedTimeAfter(String userName, LocalDateTime localDateTime);
}
