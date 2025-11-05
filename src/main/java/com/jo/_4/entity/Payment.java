package com.jo._4.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "payment")
public class Payment {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "price")
    private double price;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "payedat")
    private LocalDateTime payedAt;

    @Column(nullable = false , unique = true, updatable = false , name = "security_key")
    private UUID securityKey;

    @OneToOne
    @JoinColumn(name = "order_id")  // colonne FK dans payment
    private Order order;

    @PrePersist
    public void prePersist() {
        if (securityKey == null) {
            securityKey = UUID.randomUUID();
        }
    }
}
