package com.communication_service.communication_service.app.repository;

import com.communication_service.communication_service.app.model.dbs.FileStorageModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileStorageRepository extends JpaRepository<FileStorageModel,String> {
}
