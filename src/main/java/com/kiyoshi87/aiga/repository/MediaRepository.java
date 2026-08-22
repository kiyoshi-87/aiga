package com.kiyoshi87.aiga.repository;

import com.kiyoshi87.aiga.model.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media, Long> {
}
