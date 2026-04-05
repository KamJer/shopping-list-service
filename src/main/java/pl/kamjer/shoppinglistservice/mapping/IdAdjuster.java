package pl.kamjer.shoppinglistservice.mapping;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class IdAdjuster {

    @Named("clientLongToId")
    public Long clientLongToId(long id) {
        return id > 0 ? id : null;
    }
}
