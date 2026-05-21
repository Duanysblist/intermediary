package com.dduany.intermediary.domain.dto;

import com.dduany.intermediary.domain.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be 200 characters or fewer")
    private String title;

    @NotBlank(message = "Path is required")
    @Size(max = 500, message = "Path must be 500 characters or fewer")
    private String path;

    private DocumentType type;

    private String version;

    @Size(max = 2000, message = "Notes must be 2000 characters or fewer")
    private String notes;
}
