package com.likelion.manyfast.domain.history;

import com.likelion.manyfast.domain.history.dto.MessageHistoryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageHistoryServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-08-16T10:30:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    @Mock
    private MessageHistoryRepository messageHistoryRepository;

    @Test
    void savesMessageHistory() {
        MessageHistoryService service = service();
        when(messageHistoryRepository.save(any(MessageHistory.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 1L));

        MessageHistoryResponse response = service.save("Original", "Result");

        assertThat(response.id()).isEqualTo(1L);
        verify(messageHistoryRepository).save(any(MessageHistory.class));
    }

    @Test
    void usesFixedClockWhenSaving() {
        MessageHistoryService service = service();
        when(messageHistoryRepository.save(any(MessageHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MessageHistoryResponse response = service.save("Original", "Result");

        assertThat(response.createdAt()).isEqualTo(FIXED_INSTANT);
    }

    @Test
    void preservesOriginalAndResultTextWhenSaving() {
        MessageHistoryService service = service();
        when(messageHistoryRepository.save(any(MessageHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MessageHistoryResponse response = service.save("Can you send this by EOD?", "Please send this by EOD.");

        assertThat(response.originalText()).isEqualTo("Can you send this by EOD?");
        assertThat(response.resultText()).isEqualTo("Please send this by EOD.");
    }

    @Test
    void findsHistoryUsingCreatedAtAndIdDescendingSort() {
        MessageHistoryService service = service();
        when(messageHistoryRepository.findAll(any(Sort.class))).thenReturn(List.of());

        service.findHistory();

        ArgumentCaptor<Sort> captor = ArgumentCaptor.forClass(Sort.class);
        verify(messageHistoryRepository).findAll(captor.capture());
        assertThat(captor.getValue().toList()).containsExactly(
                new Sort.Order(Sort.Direction.DESC, "createdAt"),
                new Sort.Order(Sort.Direction.DESC, "id")
        );
    }

    @Test
    void mapsEntitiesToResponsesWhenFindingHistory() {
        MessageHistoryService service = service();
        MessageHistory stored = history(2L, "Original", "Result", FIXED_INSTANT);
        when(messageHistoryRepository.findAll(any(Sort.class))).thenReturn(List.of(stored));

        List<MessageHistoryResponse> responses = service.findHistory();

        assertThat(responses).containsExactly(new MessageHistoryResponse(
                2L,
                "Original",
                "Result",
                FIXED_INSTANT
        ));
    }

    @Test
    void returnsEmptyHistory() {
        MessageHistoryService service = service();
        when(messageHistoryRepository.findAll(any(Sort.class))).thenReturn(List.of());

        List<MessageHistoryResponse> responses = service.findHistory();

        assertThat(responses).isEmpty();
    }

    @Test
    void deletesExistingHistory() {
        MessageHistoryService service = service();
        MessageHistory stored = history(1L, "Original", "Result", FIXED_INSTANT);
        when(messageHistoryRepository.findById(1L)).thenReturn(Optional.of(stored));

        service.delete(1L);

        verify(messageHistoryRepository).delete(stored);
    }

    @Test
    void throwsWhenHistoryDoesNotExist() {
        MessageHistoryService service = service();
        when(messageHistoryRepository.findById(999999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(999999L))
                .isInstanceOf(MessageHistoryNotFoundException.class)
                .hasMessage("Message history not found: 999999");
        verify(messageHistoryRepository, never()).delete(any(MessageHistory.class));
    }

    @Test
    void deletesAllHistory() {
        MessageHistoryService service = service();

        service.deleteAll();

        verify(messageHistoryRepository).deleteAllInBatch();
    }

    @Test
    void deletesAllWhenHistoryIsEmpty() {
        MessageHistoryService service = service();

        service.deleteAll();

        verify(messageHistoryRepository).deleteAllInBatch();
    }

    @Test
    void neverUsesEntityByEntityDeleteForDeleteAll() {
        MessageHistoryService service = service();

        service.deleteAll();

        verify(messageHistoryRepository).deleteAllInBatch();
        verify(messageHistoryRepository, never()).deleteAll();
    }

    private MessageHistoryService service() {
        return new MessageHistoryService(messageHistoryRepository, FIXED_CLOCK);
    }

    private MessageHistory history(Long id, String originalText, String resultText, Instant createdAt) {
        return withId(new MessageHistory(originalText, resultText, createdAt), id);
    }

    private MessageHistory withId(MessageHistory messageHistory, Long id) {
        ReflectionTestUtils.setField(messageHistory, "id", id);
        return messageHistory;
    }
}
