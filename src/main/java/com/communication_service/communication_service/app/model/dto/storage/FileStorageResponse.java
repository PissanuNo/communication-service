package com.communication_service.communication_service.app.model.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileStorageResponse {
    private String filename;
    private String content;
    private String fileType;
}
