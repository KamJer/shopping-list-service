package pl.kamjer.shoppinglistservice.service;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public LocalDateTime insertUser(UserDto userDto) {
        User user = DatabaseUtil.toUser(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return LocalDateTime.now();
    }

    @PreAuthorize("#userDto.userName == authentication.principal.username")
    public void updateUser(UserDto userDto) {
        User user = DatabaseUtil.toUser(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @PreAuthorize("#userName == authentication.principal.username")
    public void deleteUser(String userName) throws NoResourcesFoundException {
        userRepository.delete(userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No such User")));
    }
    public LocalDateTime getLastUpdateTime() throws NoResourcesFoundException {
        return getUserFromAuth().getSavedTime();
    }

    public boolean logUser(String userName) {
        return userRepository.existsByUserName(userName);
    }

    private User getUserFromAuth() throws NoResourcesFoundException {
        String userName = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No such User"));
    }


}
