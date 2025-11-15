package pl.kamjer.shoppinglistservice.client;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
@Log4j2
public class SecClient {

    private final RestClient userRestClient;

    public LocalDateTime postUser(UserDto userDto) {
        return userRestClient
                .post()
                .body(userDto)
                .retrieve()
                .body(LocalDateTime.class);
    }

    public void putUser(UserDto userDto, String auth) {
        userRestClient
                .put()
                .uri("/savedTime")
                .header("Authorization", auth)
                .body(userDto)
                .retrieve()
                .toBodilessEntity();
    }

    public Boolean logUser(UserDto userDto) {
        return userRestClient
                .post()
                .uri("/log")
                .body(userDto)
                .retrieve()
                .body(Boolean.class);
    }

    public UserDto getUserByUserName(String userName) {
        return userRestClient
                .get()
                .uri("/{userName}", userName)
                .retrieve()
                .body(UserDto.class);
    }
}
