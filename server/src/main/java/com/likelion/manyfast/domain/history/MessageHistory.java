package com.likelion.manyfast.domain.history;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "message_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_text", nullable = false, columnDefinition = "TEXT")
    private String originalText;

    @Column(name = "result_text", nullable = false, columnDefinition = "TEXT")
    private String resultText;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant createdAt;

    MessageHistory(String originalText, String resultText, Instant createdAt) {
        this.originalText = originalText;
        this.resultText = resultText;
        this.createdAt = createdAt;
    }
}
