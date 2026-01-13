package pl.kamjer.shoppinglistservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.exception.LogException;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.ExceptionDto;

@Service
@Log4j2
public class ExceptionService extends CustomService{

    public ExceptionService(SecClient secClient, ObjectMapper objectMapper) {
        super(secClient, objectMapper);
    }

    public void insertLog(ExceptionDto e) {
        log.error("{}: ", getUserFromAuth().getUserName(), new LogException(e));
    }
}
