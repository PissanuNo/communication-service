package com.communication_service.communication_service.app.controller;


import com.communication_service.communication_service.app.model.dto.email.EmailRequest;
import com.communication_service.communication_service.app.services.EmailService;
import com.communication_service.communication_service.core.model.ResponseBodyModel;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("v2")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping(
            path = "/email",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<String>> auth(@Valid @RequestBody EmailRequest request) throws MessagingException {
        ResponseBodyModel<String> response = emailService.sendEmail(request);
        return ResponseEntity.ok(response);
    }
}
