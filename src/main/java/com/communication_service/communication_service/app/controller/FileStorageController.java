package com.communication_service.communication_service.app.controller;


import com.communication_service.communication_service.app.model.dto.storage.FileStorageRequest;
import com.communication_service.communication_service.app.model.dto.storage.FileStorageResponse;
import com.communication_service.communication_service.app.services.FileStorageService;
import com.communication_service.communication_service.core.model.ResponseBodyModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("v2")
@RequiredArgsConstructor
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @PostMapping(path = "/storage/download",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseBodyModel<FileStorageResponse>> readBlobFile(@Valid @RequestBody FileStorageRequest request) {
        ResponseBodyModel<FileStorageResponse> response = fileStorageService.downloadFileStorage(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/storage/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseBodyModel<String>> writeBlobFile(
            @RequestParam("fileContent") MultipartFile fileContent,
            @RequestParam("container") String container) {
        ResponseBodyModel<String> response = fileStorageService.uploadFileStorage(fileContent, container);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/storage/delete",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseBodyModel<String>> deleteBlobFile(@Valid @RequestBody FileStorageRequest request) {
        ResponseBodyModel<String> response = fileStorageService.deleteFileStorage(request);
        return ResponseEntity.ok(response);
    }
}
