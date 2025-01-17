package pl.kamjer.shoppinglistservice.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.service.UtilService;

@RestController
@RequestMapping(path = "/util")
@AllArgsConstructor
public class UtilController {

    private UtilService utilService;

    @Deprecated
    @PostMapping
    public ResponseEntity<AllDto> synchronizeData(@RequestBody AllDto allDto){
        return ResponseEntity.ok(utilService.synchronizeDto(allDto));
    }


}
