package com.firisbe.model;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

@Data
@Builder
@Document(collection = "log")
@NoArgsConstructor
@AllArgsConstructor
public class Log {
    @Id
    private String id;

    private Date created;

    private String logMessage;
}
