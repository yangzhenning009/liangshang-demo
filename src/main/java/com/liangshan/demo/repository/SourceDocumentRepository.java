package com.liangshan.demo.repository;

import com.liangshan.demo.domain.SourceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SourceDocumentRepository extends JpaRepository<SourceDocument, Long> {

    List<SourceDocument> findAllByOrderByIdDesc();
}
