package pl.kamjer.shoppinglistservice.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.service.UserService;

import java.time.LocalDateTime;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/user")
@Slf4j
public class UserController {

    private UserService userService;

    @PutMapping
    public ResponseEntity<?> putUser(@RequestBody UserDto user) {
        userService.updateUser(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<LocalDateTime> postUser(@Valid @RequestBody UserDto user) {
        return ResponseEntity.ok(userService.insertUser(user));
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteUser() throws NoResourcesFoundException {
        userService.deleteUser();
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<LocalDateTime> getLastUpdate() throws NoResourcesFoundException {
        return ResponseEntity.ok(userService.getLastUpdateTime());
    }

    @GetMapping(path = "/log/{userName}")
    public ResponseEntity<Boolean> logUser(@PathVariable String userName) {
        return ResponseEntity.ok(userService.logUser(userName));
    }
}
