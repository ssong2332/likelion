package com.likelion.manyfast.domain.history;

import com.likelion.manyfast.domain.history.dto.MessageHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageHistoryService {

    private static final Sort LATEST_FIRST = Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")
    );

    private final MessageHistoryRepository messageHistoryRepository;
    private final Clock clock;

    public List<MessageHistoryResponse> findHistory() {
        return messageHistoryRepository.findAll(LATEST_FIRST).stream()
                .map(MessageHistoryResponse::from)
                .toList();
    }

    @Transactional
    public MessageHistoryResponse save(String originalText, String resultText) {
        MessageHistory messageHistory = new MessageHistory(originalText, resultText, clock.instant());
        return MessageHistoryResponse.from(messageHistoryRepository.save(messageHistory));
    }

    @Transactional
    public void delete(Long id) {
        MessageHistory messageHistory = messageHistoryRepository.findById(id)
                .orElseThrow(() -> new MessageHistoryNotFoundException(id));
        messageHistoryRepository.delete(messageHistory);
    }

    @Transactional
    public void deleteAll() {
        messageHistoryRepository.deleteAllInBatch();
    }
}
