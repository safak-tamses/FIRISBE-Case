package com.firisbe.model;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "log")
public class Log {
    @Id
    private String id;

    @CreatedDate
    private OffsetDateTime created;

    @LastModifiedDate
    private OffsetDateTime updated;
    private String logMessage;
}
