package pl.kamjer.shoppinglistservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.*;

@Service
@AllArgsConstructor
public class CustomService {

    protected SecClient secClient;
    protected ObjectMapper objectMapper;

    public User getUserFromAuth() {
        String userName = ((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        String token = (String) SecurityContextHolder.getContext().getAuthentication().getDetails();
        return objectMapper.convertValue(secClient.getUserByUserName(userName, token), User.class);
    }
}
