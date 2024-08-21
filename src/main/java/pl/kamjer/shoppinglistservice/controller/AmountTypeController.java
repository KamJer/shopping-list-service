package pl.kamjer.shoppinglistservice.controller;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AddDto;
import pl.kamjer.shoppinglistservice.service.AmountTypeService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/amountType")
public class AmountTypeController {

    private AmountTypeService amountTypeService;

    @PostMapping
    public ResponseEntity<AddDto> postAmountType(@RequestBody AmountTypeDto amountTypeDto) throws NoResourcesFoundException {
        return ResponseEntity.ok(amountTypeService.insertAmountType(amountTypeDto));
    }

    @PutMapping
    public ResponseEntity<?> putAmountType(@RequestBody AmountTypeDto amountTypeDto) throws NoResourcesFoundException {
        return ResponseEntity.ok(amountTypeService.updateAmountType(amountTypeDto));
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteAmountType(@RequestParam Long amountTypeId) throws NoResourcesFoundException {
        return ResponseEntity.ok(amountTypeService.deleteAmountType(amountTypeId));
    }
}
