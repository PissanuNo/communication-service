package com.communication_service.communication_service.app.model.dto.storage;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileStorageRequest {
    @NotBlank
    private String fileId;
    @NotBlank
    private String container;
}
