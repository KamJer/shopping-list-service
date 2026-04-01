package pl.kamjer.shoppinglistservice.client;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.kamjer.shoppinglistservice.config.security.UserInfo;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
@Log4j2
public class SecClient {

    private final RestClient userRestClient;

    public void putUser(UserDto userDto, String accessToken) {
        userRestClient
                .put()
                .uri("/savedTime")
                .header("Authorization", "Bearer " + accessToken)
                .body(userDto)
                .retrieve()
                .toBodilessEntity();
    }

    public UserInfo isValid(String token) {
        return userRestClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder.path("").queryParam("token", token).build())
                .retrieve()
                .body(UserInfo.class);
    }

    public UserDto getUserByUserName(String userName, String accessToken) {
        return userRestClient
                .get()
                .uri("/{userName}", userName)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(UserDto.class);
    }
}
