package com.sabrine.medicalchatbot.repository;

import com.sabrine.medicalchatbot.Entity.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {
    List<Chunk> searchByEmbedding(float[] qVector, int i);
}
