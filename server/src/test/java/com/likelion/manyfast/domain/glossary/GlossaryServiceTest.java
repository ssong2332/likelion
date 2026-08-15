package com.likelion.manyfast.domain.glossary;

import com.likelion.manyfast.domain.glossary.dto.GlossaryRequest;
import com.likelion.manyfast.domain.glossary.dto.GlossaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlossaryServiceTest {

    @Mock
    private GlossaryRepository glossaryRepository;

    @InjectMocks
    private GlossaryService glossaryService;

    @Test
    void createsGlossary() {
        GlossaryRequest request = new GlossaryRequest("EOD", "End of Day", "업무 종료 전까지");
        when(glossaryRepository.existsByTerm("EOD")).thenReturn(false);
        when(glossaryRepository.saveAndFlush(any(Glossary.class))).thenAnswer(invocation -> {
            Glossary glossary = invocation.getArgument(0);
            ReflectionTestUtils.setField(glossary, "id", 1L);
            return glossary;
        });

        GlossaryResponse response = glossaryService.create(request);

        assertThat(response).isEqualTo(new GlossaryResponse(1L, "EOD", "End of Day", "업무 종료 전까지"));
        ArgumentCaptor<Glossary> captor = ArgumentCaptor.forClass(Glossary.class);
        verify(glossaryRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getTerm()).isEqualTo("EOD");
        assertThat(captor.getValue().getRule()).isEqualTo("End of Day");
        assertThat(captor.getValue().getNote()).isEqualTo("업무 종료 전까지");
    }

    @Test
    void findsAllGlossariesOrderedByIdAscending() {
        Glossary first = glossary(1L, "ASAP", "As Soon As Possible", null);
        Glossary second = glossary(2L, "EOD", "End of Day", "업무 종료 전까지");
        Sort idAscending = Sort.by(Sort.Direction.ASC, "id");
        when(glossaryRepository.findAll(idAscending)).thenReturn(List.of(first, second));

        List<GlossaryResponse> responses = glossaryService.findAll();

        assertThat(responses).extracting(GlossaryResponse::id).containsExactly(1L, 2L);
        verify(glossaryRepository).findAll(idAscending);
    }

    @Test
    void updatesGlossary() {
        Glossary existing = glossary(1L, "EOD", "End of Day", "기존 메모");
        GlossaryRequest request = new GlossaryRequest("COB", "Close of Business", null);
        when(glossaryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(glossaryRepository.existsByTermAndIdNot("COB", 1L)).thenReturn(false);
        when(glossaryRepository.saveAndFlush(existing)).thenReturn(existing);

        GlossaryResponse response = glossaryService.update(1L, request);

        assertThat(response).isEqualTo(new GlossaryResponse(1L, "COB", "Close of Business", null));
    }

    @Test
    void deletesGlossary() {
        Glossary existing = glossary(1L, "EOD", "End of Day", null);
        when(glossaryRepository.findById(1L)).thenReturn(Optional.of(existing));

        glossaryService.delete(1L);

        verify(glossaryRepository).delete(existing);
    }

    @Test
    void rejectsUpdateWhenGlossaryDoesNotExist() {
        GlossaryRequest request = new GlossaryRequest("EOD", "End of Day", null);
        when(glossaryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> glossaryService.update(999L, request))
                .isInstanceOf(GlossaryNotFoundException.class)
                .hasMessage("Glossary not found: 999");
    }

    @Test
    void rejectsDeleteWhenGlossaryDoesNotExist() {
        when(glossaryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> glossaryService.delete(999L))
                .isInstanceOf(GlossaryNotFoundException.class)
                .hasMessage("Glossary not found: 999");
    }

    @Test
    void rejectsDuplicateTermOnCreate() {
        GlossaryRequest request = new GlossaryRequest("EOD", "End of Day", null);
        when(glossaryRepository.existsByTerm("EOD")).thenReturn(true);

        assertThatThrownBy(() -> glossaryService.create(request))
                .isInstanceOf(DuplicateGlossaryTermException.class)
                .hasMessage("Glossary term already exists: EOD");
        verify(glossaryRepository, never()).saveAndFlush(any(Glossary.class));
    }

    @Test
    void rejectsDuplicateTermOnUpdate() {
        Glossary existing = glossary(1L, "EOD", "End of Day", null);
        GlossaryRequest request = new GlossaryRequest("ASAP", "As Soon As Possible", null);
        when(glossaryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(glossaryRepository.existsByTermAndIdNot("ASAP", 1L)).thenReturn(true);

        assertThatThrownBy(() -> glossaryService.update(1L, request))
                .isInstanceOf(DuplicateGlossaryTermException.class)
                .hasMessage("Glossary term already exists: ASAP");
        verify(glossaryRepository, never()).saveAndFlush(any(Glossary.class));
    }

    @Test
    void allowsUpdateKeepingCurrentTerm() {
        Glossary existing = glossary(1L, "EOD", "End of Day", "기존 메모");
        GlossaryRequest request = new GlossaryRequest("EOD", "End of business day", null);
        when(glossaryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(glossaryRepository.existsByTermAndIdNot("EOD", 1L)).thenReturn(false);
        when(glossaryRepository.saveAndFlush(existing)).thenReturn(existing);

        GlossaryResponse response = glossaryService.update(1L, request);

        assertThat(response).isEqualTo(new GlossaryResponse(1L, "EOD", "End of business day", null));
    }

    @Test
    void convertsDatabaseUniqueViolationToDuplicateTermException() {
        GlossaryRequest request = new GlossaryRequest("EOD", "End of Day", null);
        when(glossaryRepository.existsByTerm("EOD")).thenReturn(false);
        when(glossaryRepository.saveAndFlush(any(Glossary.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> glossaryService.create(request))
                .isInstanceOf(DuplicateGlossaryTermException.class)
                .hasMessage("Glossary term already exists: EOD")
                .hasCauseInstanceOf(DataIntegrityViolationException.class);
    }

    private Glossary glossary(Long id, String term, String rule, String note) {
        Glossary glossary = new Glossary(term, rule, note);
        ReflectionTestUtils.setField(glossary, "id", id);
        return glossary;
    }
}
