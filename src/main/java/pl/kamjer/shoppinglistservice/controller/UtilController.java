package pl.kamjer.shoppinglistservice.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllIdDto;
import pl.kamjer.shoppinglistservice.service.UtilService;

import java.time.LocalDateTime;

@RestController
@RequestMapping(path = "/util")
@AllArgsConstructor
public class UtilController {

    private UtilService utilService;

    @GetMapping(path = "/message")
    public ResponseEntity<String> getMessage() {
        return ResponseEntity.status(HttpStatus.OK).body("Kocham cię");
    }

    @PostMapping
    public ResponseEntity<AllDto> synchronizeData(@RequestBody AllDto allDto){
        return ResponseEntity.ok(utilService.synchronizeDto(allDto));
    }


}
