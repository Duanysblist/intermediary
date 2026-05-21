package com.dduany.intermediary.document.dto;

import com.dduany.intermediary.document.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {

    private Long id;

    private String title;

    private String path;

    private DocumentType type;

    private String version;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
