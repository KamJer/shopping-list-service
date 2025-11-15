package pl.kamjer.shoppinglistservice.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.service.UserService;

import java.time.LocalDateTime;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/user")
@Slf4j
public class UserController {

    private UserService userService;

    @PostMapping(path = "/log")
    public ResponseEntity<Boolean> logUser(@RequestBody UserDto user) {
        return ResponseEntity.ok(userService.logUser(user));
    }
}
