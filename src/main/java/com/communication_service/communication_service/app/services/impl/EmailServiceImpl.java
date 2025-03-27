package com.communication_service.communication_service.app.services.impl;

import com.communication_service.communication_service.app.model.dto.email.EmailRequest;
import com.communication_service.communication_service.app.services.EmailService;
import com.communication_service.communication_service.core.model.ResponseBodyModel;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.communication_service.communication_service.app.constants.Constant.ResponseCode.INTERNAL_SERVER_ERROR;
import static com.communication_service.communication_service.app.constants.Constant.ResponseCode.SUCCESS_CODE;
import static com.communication_service.communication_service.app.constants.Constant.ResponseMessage.INTERNAL_SERVER_ERROR_MSG;
import static com.communication_service.communication_service.app.constants.Constant.ResponseMessage.SUCCESS;
import static com.communication_service.communication_service.app.constants.Email.Subject.*;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender emailSender;
    private final ApplicationContext applicationContext;

    @Value("${sender.email}")
    private String senderEmail;

    @Override
    public ResponseBodyModel<String> sendEmail(EmailRequest request) {
        ResponseBodyModel<String> response = new ResponseBodyModel<>();
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            //replace variable in email template
            String processedHtml = replaceVariables(request.getTemplateName(), request.getVariables());
            setSubject(request);

            helper.setTo(request.getRecipientEmail());
            helper.setSubject(request.getSubject());
            helper.setText(processedHtml, true);
            helper.setFrom(senderEmail);

            emailSender.send(message);
            response.setOperationSuccess(SUCCESS_CODE, SUCCESS, null);
        } catch (Exception ex) {
            log.error("Error send email: ", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }

    private String replaceVariables(String templateName, Map<String, Object> variables) {
        if (templateName == null || variables == null) {
            return templateName;
        }
        String templateEmail = getTemplateContent(templateName);
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            templateEmail = templateEmail.replaceAll(Pattern.quote("{{" + entry.getKey() + "}}"),
                    Matcher.quoteReplacement(entry.getValue().toString()));
        }
        return templateEmail;
    }

    public String getTemplateContent(String templateFile) {
        String fullPath = "email/" + templateFile + ".html";
        Resource resource = applicationContext.getResource("classpath:" + fullPath);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading email template: " + fullPath, e);
        }
    }

    public void setSubject(EmailRequest request) {
        switch (request.getTemplateName()) {
            case "reset_password":
                request.setSubject(RESET_PASSWORD);
                break;
            case "activate_account":
                request.setSubject(ACTIVATE_ACCOUNT);
                break;
            default:
                request.setSubject(DEFAULT_SUBJECT);
        }
    }
}
