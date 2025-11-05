package com.jo._4.repository;

import com.jo._4.entity.Cart;
import com.jo._4.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {


    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByUser(User user);

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findBySessionId(String sessionId);
}
