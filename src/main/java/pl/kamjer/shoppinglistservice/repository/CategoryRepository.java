package pl.kamjer.shoppinglistservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamjer.shoppinglistservice.model.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findCategoryByUserUserNameAndCategoryId(String userName, long categoryId_categoryId);
    List<Category> findByUserUserName(String userName);
    List<Category> findCategoryByUserUserNameAndSavedTimeAfter(String userName, LocalDateTime localDateTime);
}
