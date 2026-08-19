package com.likelion.manyfast.domain.glossary;

import com.likelion.manyfast.domain.glossary.dto.GlossaryRequest;
import com.likelion.manyfast.domain.glossary.dto.GlossaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GlossaryService {

    private static final Sort ID_ASC = Sort.by(Sort.Direction.ASC, "id");

    private final GlossaryRepository glossaryRepository;

    @Transactional
    public GlossaryResponse create(GlossaryRequest request) {
        if (glossaryRepository.existsByTerm(request.term())) {
            throw new DuplicateGlossaryTermException(request.term());
        }

        Glossary glossary = new Glossary(request.term(), request.rule(), request.note());
        return save(glossary);
    }

    public List<GlossaryResponse> findAll() {
        return glossaryRepository.findAll(ID_ASC).stream()
                .map(GlossaryResponse::from)
                .toList();
    }

    public List<GlossaryResponse> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>();
        for (Long id : ids) {
            validateReferenceId(id);
            uniqueIds.add(id);
        }

        Map<Long, Glossary> glossariesById = glossaryRepository.findAllById(uniqueIds).stream()
                .collect(Collectors.toMap(Glossary::getId, Function.identity()));

        return uniqueIds.stream()
                .map(id -> {
                    Glossary glossary = glossariesById.get(id);
                    if (glossary == null) {
                        throw new GlossaryNotFoundException(id);
                    }
                    return GlossaryResponse.from(glossary);
                })
                .toList();
    }

    @Transactional
    public GlossaryResponse update(Long id, GlossaryRequest request) {
        Glossary glossary = findById(id);
        if (glossaryRepository.existsByTermAndIdNot(request.term(), id)) {
            throw new DuplicateGlossaryTermException(request.term());
        }

        glossary.update(request.term(), request.rule(), request.note());
        return save(glossary);
    }

    @Transactional
    public void delete(Long id) {
        Glossary glossary = findById(id);
        glossaryRepository.delete(glossary);
    }

    private Glossary findById(Long id) {
        return glossaryRepository.findById(id)
                .orElseThrow(() -> new GlossaryNotFoundException(id));
    }

    private GlossaryResponse save(Glossary glossary) {
        try {
            return GlossaryResponse.from(glossaryRepository.saveAndFlush(glossary));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateGlossaryTermException(glossary.getTerm(), exception);
        }
    }

    private void validateReferenceId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("glossary id is required");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("glossary id must be positive");
        }
    }
}
