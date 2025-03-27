package com.communication_service.communication_service.app.model.dto.email;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
    @NotBlank
    private String recipientEmail;
    private String subject;
    @NotBlank
    private String templateName;

    private Map<String, Object> variables;
}
