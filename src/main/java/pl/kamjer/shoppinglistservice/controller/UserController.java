package pl.kamjer.shoppinglistservice.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.service.UserService;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/user")
@Log
public class UserController {

    private UserService userService;

    @PutMapping
    public ResponseEntity<?> putUser(@RequestBody UserDto user) {
        userService.updateUser(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> postUser(@Valid @RequestBody UserDto user) {
        userService.insertUser(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/{userName}")
    public ResponseEntity<?> deleteUser(@PathVariable String userName) throws NoResourcesFoundException {
        userService.deleteUser(userName);
        return ResponseEntity.ok().build();
    }
}
