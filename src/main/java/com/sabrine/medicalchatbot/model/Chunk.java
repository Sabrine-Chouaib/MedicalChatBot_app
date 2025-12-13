package com.sabrine.medicalchatbot.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String content;

    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;
}
