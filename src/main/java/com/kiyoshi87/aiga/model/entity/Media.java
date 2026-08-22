package com.kiyoshi87.aiga.model.entity;

import com.kiyoshi87.aiga.model.SourceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "media")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SourceType sourceType;

    @NotBlank
    @Column(nullable = false, length = 2_048)
    private String sourceUrl;
    private String title;
    private Long duration;
}
