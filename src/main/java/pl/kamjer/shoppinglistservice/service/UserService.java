package pl.kamjer.shoppinglistservice.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public void insertUser(UserDto userDto) {
        User user = DatabaseUtil.toUser(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @PreAuthorize("#userDto.userName == authentication.principal.username")
    public void updateUser(UserDto userDto) {
        User user = DatabaseUtil.toUser(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @PreAuthorize("#userName == authentication.principal.username")
    public UserDto findUserById(String userName) throws NoResourcesFoundException {
        return DatabaseUtil.toUserDto(userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No such User")));
    }

    @PreAuthorize("#userName == authentication.principal.username")
    public void deleteUser(String userName) throws NoResourcesFoundException {
        userRepository.delete(userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No such User")));
    }
}
