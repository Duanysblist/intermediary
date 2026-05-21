package com.dduany.intermediary.domain;

import com.dduany.intermediary.domain.dto.DocumentRequest;
import com.dduany.intermediary.domain.dto.DocumentResponse;

import com.dduany.intermediary.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DocumentResponse create(DocumentRequest request){
        Document document = DocumentMapper.toEntity(request);
        Document saved = repository.save(document);
        return DocumentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DocumentResponse findById(Long id){
        Document document = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found with id " + id
                ));
        return DocumentMapper.toResponse(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> findAll(){
        return repository.findAll().stream()
                .map(DocumentMapper::toResponse)
                .toList();
    }

    // TODO: Use preserve-existing. Look at Application Service.
    @Transactional
    public DocumentResponse update(Long id, DocumentRequest request){
        Document existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found with id " + id
        ));

        existing.setTitle(request.getTitle());
        existing.setPath(request.getPath());
        existing.setType(request.getType() != null ? request.getType() : existing.getType());
        existing.setVersion(request.getVersion() !=  null ? request.getVersion() : existing.getVersion());
        existing.setNotes(request.getNotes());

        Document saved = repository.save(existing);
        return DocumentMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Document not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
