package com.liangshan.demo.web;

import com.liangshan.demo.service.SourceDocumentService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class DocumentOptions {

    private final SourceDocumentService documents;

    public DocumentOptions(SourceDocumentService documents) {
        this.documents = documents;
    }

    @ModelAttribute("sourceDocuments")
    public List<SourceDocumentService.Option> sourceDocuments() {
        return documents.options();
    }
}
