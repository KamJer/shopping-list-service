package pl.kamjer.shoppinglistservice.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.exception.LogException;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.ExceptionDto;

@Service
@Log4j2
public class ExceptionService extends CustomService{

    public ExceptionService(SecClient secClient) {
        super(secClient);
    }

    public void insertLog(ExceptionDto e) {
        log.error("{}: ", getUserFromAuth().getUserName(), new LogException(e));
    }
}
