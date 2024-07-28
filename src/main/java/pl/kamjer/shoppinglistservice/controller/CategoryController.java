package pl.kamjer.shoppinglistservice.controller;

import jakarta.persistence.PostUpdate;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.CategoryId;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.service.CategoryService;
import pl.kamjer.shoppinglistservice.service.UserService;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/category")
public class CategoryController {

    private CategoryService categoryService;

    @GetMapping(path = "/{userName}")
    public ResponseEntity<List<CategoryDto>> getCategoryByUser(@PathVariable String userName) throws NoResourcesFoundException {
        return ResponseEntity.ok(categoryService.getCategoryByUser(userName));
    }

    @PutMapping
    public ResponseEntity<?> putCategory(@RequestBody CategoryDto category) throws NoResourcesFoundException {
        categoryService.updateCategory(category);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Long> postCategory(@RequestBody CategoryDto categoryDto) throws NoResourcesFoundException {
        return ResponseEntity.ok(categoryService.insertCategory(categoryDto));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCategory(@RequestBody CategoryDto categoryDto) throws NoResourcesFoundException {
        categoryService.deleteCategory(categoryDto);
        return ResponseEntity.ok().build();
    }
}
