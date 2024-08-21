package pl.kamjer.shoppinglistservice.controller;

import jakarta.persistence.PostUpdate;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.Category;
import pl.kamjer.shoppinglistservice.model.CategoryId;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AddDto;
import pl.kamjer.shoppinglistservice.service.CategoryService;
import pl.kamjer.shoppinglistservice.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/category")
public class CategoryController {

    private CategoryService categoryService;

    @PutMapping
    public ResponseEntity<?> putCategory(@RequestBody CategoryDto category) throws NoResourcesFoundException {
        return ResponseEntity.ok(categoryService.updateCategory(category));
    }

    @PostMapping
    public ResponseEntity<AddDto> postCategory(@RequestBody CategoryDto categoryDto) throws NoResourcesFoundException {
        return ResponseEntity.ok(categoryService.insertCategory(categoryDto));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCategory(@RequestParam Long categoryId) throws NoResourcesFoundException {
        return ResponseEntity.ok(categoryService.deleteCategory(categoryId));
    }
}
