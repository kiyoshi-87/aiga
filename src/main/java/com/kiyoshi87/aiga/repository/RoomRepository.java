package com.kiyoshi87.aiga.repository;

import com.kiyoshi87.aiga.model.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
