package pl.kamjer.shoppinglistservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.model.User;

import java.util.Optional;

@Service
@AllArgsConstructor
@Log4j2
public class CustomService {

    protected SecClient secClient;
    protected ObjectMapper objectMapper;

    public Optional<User> getUserFromAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof String userName)) {
            return Optional.empty();
        }
        Object details = authentication.getDetails();
        if (!(details instanceof String token)) {
            return Optional.empty();
        }
        try {
            User user = objectMapper.convertValue(secClient.getUserByUserName(userName, token), User.class);
            return Optional.ofNullable(user);
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            if (status == 401 || status == 403) {
                log.debug("Sec rejected auth for userName={} (HTTP {})", userName, status, e);
            } else {
                log.warn("Sec getUser error for userName={} (HTTP {})", userName, status, e);
            }
            return Optional.empty();
        } catch (ResourceAccessException e) {
            log.warn("Sec unreachable for userName={}: {}", userName, e.toString(), e);
            return Optional.empty();
        } catch (RuntimeException e) {
            log.warn("Unexpected error loading user from Sec for userName={}", userName, e);
            return Optional.empty();
        }
    }
}
