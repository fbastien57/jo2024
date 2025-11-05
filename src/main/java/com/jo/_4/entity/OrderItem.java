package com.jo._4.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "order_item")
public class OrderItem {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Order order;

    @ManyToOne
    private Session session;

    @Column(name = "quantity")
    private int quantity;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL)
    private List<Ticket> tickets;
}
