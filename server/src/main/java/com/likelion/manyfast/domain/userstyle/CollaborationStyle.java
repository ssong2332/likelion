package com.likelion.manyfast.domain.userstyle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "collaboration_styles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollaborationStyle {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tone", nullable = false, length = 50)
    private String tone;

    @Column(name = "directness", nullable = false, length = 50)
    private String directness;

    @Column(name = "detail_level", nullable = false, length = 50)
    private String detailLevel;

    public CollaborationStyle(Long id, String tone, String directness, String detailLevel) {
        this.id = id;
        this.tone = tone;
        this.directness = directness;
        this.detailLevel = detailLevel;
    }

    public void update(String tone, String directness, String detailLevel) {
        this.tone = tone;
        this.directness = directness;
        this.detailLevel = detailLevel;
    }
}
