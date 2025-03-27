package com.communication_service.communication_service.app.services;

import com.communication_service.communication_service.app.model.dto.storage.FileStorageRequest;
import com.communication_service.communication_service.app.model.dto.storage.FileStorageResponse;
import com.communication_service.communication_service.core.model.ResponseBodyModel;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    ResponseBodyModel<String> uploadFileStorage(MultipartFile fileContent, String container);

    ResponseBodyModel<FileStorageResponse> downloadFileStorage(FileStorageRequest request);

    ResponseBodyModel<String> deleteFileStorage(FileStorageRequest request);
}
