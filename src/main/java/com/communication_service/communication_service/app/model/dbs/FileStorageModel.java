package com.communication_service.communication_service.app.model.dbs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "portal_file_storage")
public class FileStorageModel {
    @Id
    @Builder.Default
    private String fileId = UUID.randomUUID().toString();
    private String fileName;
    private String url;
    private String type;
    private String containerName;

    @Column(columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    @Builder.Default
    private Date createDate = new Timestamp(System.currentTimeMillis());
    @Builder.Default
    private String createBy = "Administrator";
    @Column(columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    @Builder.Default
    private Date modifyDate = new Timestamp(System.currentTimeMillis());
    @Builder.Default
    private String modifyBy = "Administrator";

}
