package pl.kamjer.shoppinglistservice.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.ShoppingItem;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.service.ShoppingItemService;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/shoppingItem")
public class ShoppingItemController {

    private ShoppingItemService shoppingItemService;

    @GetMapping(path = "/{userName}")
    public ResponseEntity<List<ShoppingItemDto>> getShoppingItemByUser(@PathVariable String userName) throws NoResourcesFoundException {
        return ResponseEntity.ok(shoppingItemService.getShoppingItemsByUser(userName));
    }

    @PostMapping
    public ResponseEntity<Long> postShoppingItem(@RequestBody ShoppingItemDto shoppingItemDto) throws NoResourcesFoundException {
        ;
        return ResponseEntity.ok(shoppingItemService.insertShoppingItem(shoppingItemDto));
    }

    @PutMapping
    public ResponseEntity<?> putShoppingItem(@RequestBody ShoppingItemDto shoppingItemDto) throws NoResourcesFoundException {
        shoppingItemService.updateShoppingItem(shoppingItemDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteShoppingItem(@RequestBody ShoppingItemDto shoppingItemDto) throws NoResourcesFoundException {
        shoppingItemService.deleteShoppingItem(shoppingItemDto);
        return ResponseEntity.ok().build();
    }
}
