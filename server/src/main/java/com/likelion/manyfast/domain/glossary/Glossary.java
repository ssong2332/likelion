package com.likelion.manyfast.domain.glossary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "glossaries",
        uniqueConstraints = @UniqueConstraint(name = "uk_glossaries_term", columnNames = "term")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Glossary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "term", nullable = false, length = 100)
    private String term;

    @Column(name = "rule_text", nullable = false, length = 255)
    private String rule;

    @Column(name = "note", length = 255)
    private String note;

    public Glossary(String term, String rule, String note) {
        this.term = term;
        this.rule = rule;
        this.note = note;
    }

    public void update(String term, String rule, String note) {
        this.term = term;
        this.rule = rule;
        this.note = note;
    }
}
