package pl.kamjer.shoppinglistservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.exception.LogException;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.ExceptionDto;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

import java.util.Arrays;

@Service
@Slf4j
public class ExceptionService extends CustomService{

    public ExceptionService(UserRepository userRepository) {
        super(userRepository);
    }

    public void insertLog(ExceptionDto e) {
        log.error(getUserFromAuth().getUserName() + ": ", new LogException(e));
    }
}
