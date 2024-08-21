package pl.kamjer.shoppinglistservice.controller;

import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.service.UserService;

import java.time.LocalDateTime;

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
    public ResponseEntity<LocalDateTime> postUser(@Valid @RequestBody UserDto user) {
        return ResponseEntity.ok(userService.insertUser(user));
    }

    @DeleteMapping(path = "/{userName}")
    public ResponseEntity<?> deleteUser(@PathVariable String userName) throws NoResourcesFoundException {
        userService.deleteUser(userName);
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
