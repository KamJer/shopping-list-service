package pl.kamjer.shoppinglistservice.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class CustomService {

    protected UserRepository userRepository;
    protected User updateSaveTimeInUser(LocalDateTime localDateTime) throws NoResourcesFoundException {
        User user = getUserFromAuth();
        user.setSavedTime(localDateTime);
        return user;
    }

    public User getUserFromAuth() throws NoResourcesFoundException {
        String userName = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByUserName(userName).orElseThrow(() -> new NoResourcesFoundException("No such User"));
    }
}
