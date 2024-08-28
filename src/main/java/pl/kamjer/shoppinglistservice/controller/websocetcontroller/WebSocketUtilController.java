package pl.kamjer.shoppinglistservice.controller.websocetcontroller;

import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.service.UtilService;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class WebSocketUtilController {

    private SimpMessagingTemplate messagingTemplate;
    private UtilService utilService;

    @MessageMapping("/synchronizeData")
    public void synchronizeData(AllDto allDto) {
        AllDto allDtoToSend = utilService.synchronizeDto(allDto);
        messagingTemplate.convertAndSendToUser(utilService.getUserFromAuth().getUserName(), "/queue/updates", allDtoToSend);
    }
}
