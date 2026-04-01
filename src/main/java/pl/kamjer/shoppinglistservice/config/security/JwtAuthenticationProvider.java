package pl.kamjer.shoppinglistservice.config.security;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.model.User;

import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final SecClient secClient;

    @Override
    public Authentication authenticate(Authentication authentication) {

        String token = (String) authentication.getCredentials();

        try {
            UserInfo isValid = secClient.isValid(token);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            isValid.getUserName(),
                            null,
                            List.of(new SimpleGrantedAuthority(isValid.getRole()))
                    );

            auth.setDetails(token);

            return auth;
        } catch (HttpClientErrorException ex) {
            throw new BadCredentialsException("Invalid token");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthToken.class.isAssignableFrom(authentication);
    }
}
