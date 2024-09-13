package pl.kamjer.shoppinglistservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class UserService extends CustomService {

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(userRepository);
        this.passwordEncoder = passwordEncoder;
    }

    public LocalDateTime insertUser(UserDto userDto) {
        LocalDateTime savedTime = LocalDateTime.now();
        User user = DatabaseUtil.toUser(userDto);
        user.setSavedTime(savedTime);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return LocalDateTime.now();
    }

    public void updateUser(UserDto userDto) {
        User userSec = getUserFromAuth();
        User user = DatabaseUtil.toUser(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User userToUpdate = userRepository.findByUserName(userSec.getUserName())
                .orElseThrow(() -> new NoResourcesFoundException("No such User found: " + userSec.getUserName()));
        userToUpdate.setUserName(user.getUserName());
        userToUpdate.setPassword(user.getPassword());
    }

    public void deleteUser() throws NoResourcesFoundException {
        userRepository.delete(userRepository.findByUserName(getUserFromAuth().getUserName()).orElseThrow(() -> new NoResourcesFoundException("No such User")));
    }

    public LocalDateTime getLastUpdateTime() throws NoResourcesFoundException {
        return getUserFromAuth().getSavedTime();
    }

    public boolean logUser(String userName) {
        return userRepository.existsByUserName(userName);
    }
}
