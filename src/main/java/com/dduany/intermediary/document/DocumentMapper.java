package com.dduany.intermediary.document;

import com.dduany.intermediary.document.dto.DocumentRequest;
import com.dduany.intermediary.document.dto.DocumentResponse;

public final class DocumentMapper {

    private DocumentMapper() {
        // utility class - prevent instantiation
    }

    public static Document toEntity(DocumentRequest request) {
        return Document.builder()
                .title(request.getTitle())
                .path(request.getPath())
                .type(request.getType())
                .version(request.getVersion())
                .notes(request.getNotes())
                .build();
    }

    public static DocumentResponse toResponse(Document entity) {
        return DocumentResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .path(entity.getPath())
                .type(entity.getType())
                .version(entity.getVersion())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
