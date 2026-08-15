package com.likelion.manyfast.domain.glossary;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GlossaryRepository extends JpaRepository<Glossary, Long> {

    boolean existsByTerm(String term);

    boolean existsByTermAndIdNot(String term, Long id);
}
