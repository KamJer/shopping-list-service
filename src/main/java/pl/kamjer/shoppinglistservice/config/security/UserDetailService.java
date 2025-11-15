package pl.kamjer.shoppinglistservice.config.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.client.SecClient;

@AllArgsConstructor
@Service
public class UserDetailService implements UserDetailsService {

    private final SecClient secClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return DatabaseUtil.toUser(secClient.getUserByUserName(username))
                .convertToSpringUser();
    }
}
