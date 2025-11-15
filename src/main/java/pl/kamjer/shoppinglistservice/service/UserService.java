package pl.kamjer.shoppinglistservice.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;

@Service
public class UserService extends CustomService {

    public UserService(SecClient secClient) {
        super(secClient);
        this.secClient = secClient;
    }

//    @Transactional
//    public LocalDateTime insertUser(UserDto userDto) {
//        return secClient.postUser(userDto);
//    }
//
//    @Transactional
//    public void updateUser(UserDto userDto) {
//        secClient.putUser(userDto);
//    }

    @Transactional
    public Boolean logUser(UserDto userDto) {
        return secClient.logUser(userDto);
    }
}
