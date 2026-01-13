package pl.kamjer.shoppinglistservice.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.model.User;

@AllArgsConstructor
@Service
public class UserDetailService implements UserDetailsService {

    private final SecClient secClient;
    private ObjectMapper objectMapper;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return objectMapper.convertValue(secClient.getUserByUserName(username), User.class)
                .convertToSpringUser();
    }
}
