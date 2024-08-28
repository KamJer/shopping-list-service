package pl.kamjer.shoppinglistservice.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.ExceptionDto;
import pl.kamjer.shoppinglistservice.service.ExceptionService;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/exception")
public class ExceptionController {

    private ExceptionService exceptionService;

    @PostMapping
    private ResponseEntity<Void> sendLog(@RequestBody ExceptionDto e) {
        exceptionService.insertLog(e);
        return ResponseEntity.ok().build();
    }
}
