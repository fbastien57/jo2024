package com.jo._4.repository;

import com.jo._4.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository  extends JpaRepository<Event, Long> {
    Long id(Long id);
}
