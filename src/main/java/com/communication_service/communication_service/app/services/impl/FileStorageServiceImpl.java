package com.communication_service.communication_service.app.services.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import com.communication_service.communication_service.app.model.dbs.FileStorageModel;
import com.communication_service.communication_service.app.model.dto.storage.FileStorageRequest;
import com.communication_service.communication_service.app.model.dto.storage.FileStorageResponse;
import com.communication_service.communication_service.app.repository.FileStorageRepository;
import com.communication_service.communication_service.app.services.FileStorageService;
import com.communication_service.communication_service.app.services.LoggingService;
import com.communication_service.communication_service.core.model.ResponseBodyModel;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static com.communication_service.communication_service.app.constants.Constant.ResponseCode.*;
import static com.communication_service.communication_service.app.constants.Constant.ResponseMessage.*;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    private final FileStorageRepository fileStorageRepository;
    private final LoggingService loggingService;

    @Value("${spring.cloud.azure.storage.blob.connection-string}")
    private String connectionString;


    private BlobServiceClient blobServiceClient;

    @PostConstruct
    private void initStorage() {
        blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Override
    public ResponseBodyModel<String> uploadFileStorage(MultipartFile fileContent, String container) {
        ResponseBodyModel<String> response = new ResponseBodyModel<>();
        try {
            if (fileContent.getSize() > 2048000) {
                loggingService.logStamp(ERROR_CODE_FILE_UPLOAD,
                        "Upload fail error: file size limit exceeded", FileStorageServiceImpl.class);
                response.setOperationError(ERROR_CODE_FILE_UPLOAD, ERROR_FILE_SIZE_LIMIT_EXCEEDED, null);
                return response;
            }
            String fileName = fileRename(fileContent);
            String type = FilenameUtils.getExtension(fileName);
            BlobClient blobClient = blobServiceClient
                    .getBlobContainerClient(container)
                    .getBlobClient(fileName);
            blobClient.upload(fileContent.getInputStream(), fileContent.getSize());
            String fileUrl = blobClient.getBlobUrl();
            fileStorageRepository.save(FileStorageModel
                    .builder()
                    .fileName(fileName)
                    .url(fileUrl)
                    .type(type)
                    .containerName(container)
                    .build());
            response.setOperationSuccess(SUCCESS_CODE, SUCCESS, "Upload successful.");
        } catch (BlobStorageException bex) {
            loggingService.logStamp(ERROR_CODE_FILE_UPLOAD, bex.getMessage(), FileStorageServiceImpl.class);
            response.setOperationError(ERROR_CODE_FILE_UPLOAD, ERROR_FILE_STORAGE_UPLOAD, null);
        } catch (Exception ex) {
            loggingService.logStamp(INTERNAL_SERVER_ERROR, ex.getMessage(), FileStorageServiceImpl.class);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, "Upload fail.");
        }
        return response;
    }

    @Override
    public ResponseBodyModel<FileStorageResponse> downloadFileStorage(FileStorageRequest request) {
        ResponseBodyModel<FileStorageResponse> response = new ResponseBodyModel<>();
        try {
            FileStorageResponse fileDetail = new FileStorageResponse();
            Optional<FileStorageModel> fileQuery = fileStorageRepository.findById(request.getFileId());
            if (fileQuery.isEmpty()) {
                loggingService.logStamp(ERROR_CODE_FILE_DOWNLOAD,
                        "File Not Found.", FileStorageServiceImpl.class);
                response.setOperationError(ERROR_CODE_FILE_DOWNLOAD, ERROR_FILE_NOT_FOUND, null);
                return response;
            }
            BlobContainerClient containerClient = blobServiceClient
                    .getBlobContainerClient(request.getContainer());
            BlobClient blobClient = containerClient.getBlobClient(fileQuery.get().getFileName());
            String content = Base64.getEncoder().encodeToString(blobClient.downloadContent().toBytes());
            fileDetail.setFileType(fileQuery.get().getType());
            fileDetail.setContent(content);
            fileDetail.setFilename(fileQuery.get().getFileName());
            response.setOperationSuccess(SUCCESS_CODE, SUCCESS, fileDetail);

        } catch (BlobStorageException bex) {
            loggingService.logStamp(ERROR_CODE_FILE_DOWNLOAD, bex.getMessage(), FileStorageServiceImpl.class);
            response.setOperationError(ERROR_CODE_FILE_DOWNLOAD, ERROR_FILE_STORAGE_DOWNLOAD, null);
        } catch (Exception ex) {
            loggingService.logStamp(ERROR_CODE_FILE_DOWNLOAD, ex.getMessage(), FileStorageServiceImpl.class);
            response.setOperationError(ERROR_CODE_FILE_DOWNLOAD, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }

    private String fileRename(MultipartFile fileContent) {
        String pattern = "yyyyMMddhhmmss";
        DateFormat df = new SimpleDateFormat(pattern);
        Date today = Calendar.getInstance().getTime();
        String filename = df.format(today);
        String original = fileContent.getOriginalFilename();
        assert original != null;
        filename = filename.concat("_").concat(original);
        return filename;
    }

    @Transactional
    @Override
    public ResponseBodyModel<String> deleteFileStorage(FileStorageRequest request) {
        ResponseBodyModel<String> response = new ResponseBodyModel<>();
        try {
            Optional<FileStorageModel> fileStorageModel = fileStorageRepository.findById(request.getFileId());
            BlobContainerClient containerClient = blobServiceClient
                    .getBlobContainerClient(request.getContainer());
            if (fileStorageModel.isPresent() && containerClient.exists()) {
                BlobClient blobClient = containerClient.getBlobClient(fileStorageModel.get().getFileName());
                if (Boolean.TRUE.equals(blobClient.exists())) {
                    blobClient.delete();
                    fileStorageRepository.deleteById(request.getFileId());
                    response.setOperationSuccess(SUCCESS_CODE, SUCCESS, null);
                }
            } else {
                response.setOperationError(SUCCESS_CODE, ERROR_FILE_NOT_FOUND, null);
            }
        } catch (BlobStorageException bex) {
            loggingService.logStamp(ERROR_CODE_FILE_DELETE, bex.getMessage(), FileStorageServiceImpl.class);
            response.setOperationError(ERROR_CODE_FILE_DELETE, ERROR_FILE_STORAGE_DELETE, null);
        } catch (Exception ex) {
            loggingService.logStamp(ERROR_CODE_FILE_DELETE, ex.getMessage(), FileStorageServiceImpl.class);
            response.setOperationError(ERROR_CODE_FILE_DELETE, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }
}
