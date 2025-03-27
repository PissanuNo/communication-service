package com.communication_service.communication_service.app.services;

import com.communication_service.communication_service.app.model.dto.email.EmailRequest;
import com.communication_service.communication_service.core.model.ResponseBodyModel;
import jakarta.mail.MessagingException;

public interface EmailService {

    ResponseBodyModel<String> sendEmail(EmailRequest emailRequest) throws MessagingException;
}
