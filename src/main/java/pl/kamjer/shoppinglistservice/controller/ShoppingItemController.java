package pl.kamjer.shoppinglistservice.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AddDto;
import pl.kamjer.shoppinglistservice.service.ShoppingItemService;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/shoppingItem")
public class ShoppingItemController {

    private ShoppingItemService shoppingItemService;

    @PostMapping
    public ResponseEntity<AddDto> postShoppingItem(@RequestBody ShoppingItemDto shoppingItemDto) throws NoResourcesFoundException {
        return ResponseEntity.ok(shoppingItemService.insertShoppingItem(shoppingItemDto));
    }

    @PutMapping
    public ResponseEntity<?> putShoppingItem(@RequestBody ShoppingItemDto shoppingItemDto) throws NoResourcesFoundException {
        return ResponseEntity.ok(shoppingItemService.updateShoppingItem(shoppingItemDto));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteShoppingItem(@RequestParam Long shoppingItemId) throws NoResourcesFoundException {
        return ResponseEntity.ok(shoppingItemService.deleteShoppingItem(shoppingItemId));
    }
}
