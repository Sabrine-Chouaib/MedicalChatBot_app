package com.sabrine.medicalchatbot.Entity;

import jakarta.persistence.*;
        import lombok.Data;
@Data
@Entity
@Table(name = "chunks")
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "embedding", columnDefinition = "vector")
    private float[] embedding;
}
