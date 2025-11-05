package com.jo._4.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@Setter
@Getter
public class Cart {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private String sessionId;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL , orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public boolean isAttachedToUser(){
        return user != null;
    }

    public double getTotalPrice() {
        if (items == null) return 0;
        return items.stream()
                .mapToDouble(i -> i.getSession().getPrice() * i.getQuantity())
                .sum();
    }
}

