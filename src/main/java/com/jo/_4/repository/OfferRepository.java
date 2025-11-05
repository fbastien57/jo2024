package com.jo._4.repository;

import com.jo._4.entity.Offer;
import com.jo._4.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findBySessionId(Long sessionId);
}
