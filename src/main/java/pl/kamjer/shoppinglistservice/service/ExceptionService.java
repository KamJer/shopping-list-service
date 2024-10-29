package pl.kamjer.shoppinglistservice.service;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.exception.LogException;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.ExceptionDto;
import pl.kamjer.shoppinglistservice.repository.UserRepository;

@Service
@Log4j2
public class ExceptionService extends CustomService{

    public ExceptionService(UserRepository userRepository) {
        super(userRepository);
    }

    public void insertLog(ExceptionDto e) {
        log.error(getUserFromAuth().getUserName() + ": ", new LogException(e));
    }
}
