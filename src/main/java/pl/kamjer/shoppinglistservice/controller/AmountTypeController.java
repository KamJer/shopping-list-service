package pl.kamjer.shoppinglistservice.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.AmountTypeId;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.service.AmountTypeService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/amountType")
public class AmountTypeController {

    private AmountTypeService amountTypeService;

    @GetMapping(path = "/{userName}")
    public ResponseEntity<List<AmountTypeDto>> getAmountTypeByUser(@PathVariable String userName) throws NoResourcesFoundException {
        return ResponseEntity.ok(amountTypeService.getAmountTypeByUser(userName));
    }

    @PostMapping
    public ResponseEntity<Long> postAmountType(@RequestBody AmountTypeDto amountTypeDto) throws NoResourcesFoundException {
        return ResponseEntity.ok(amountTypeService.insertAmountType(amountTypeDto));
    }

    @PutMapping
    public ResponseEntity<?> putAmountType(@RequestBody AmountTypeDto amountTypeDto) throws NoResourcesFoundException {
        amountTypeService.updateAmountType(amountTypeDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAmountType(@RequestBody AmountTypeDto amountTypeDto) throws NoResourcesFoundException {
        amountTypeService.deleteAmountType(amountTypeDto);
        return ResponseEntity.ok().build();
    }
}
