package com.jo._4.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotEmpty(message = "The name can't be empty")
    @Column(name = "name")
    private String name;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @NotNull(message = "The total capacity can't be null")
    @Positive(message = "The total capacity need to be positive")
    @Column(name = "total_capacity")
    private int totalCapacity;

    @NotNull(message = "The remaining capacity can't be null")
    @Positive(message = "The remaining capacity need to be positive")
    @Column(name = "remaining_capacity")
    private int remainingCapacity;

    @NotNull(message = "The price can't be null")
    @Positive(message = "The price need to be positive")
    @Column(name = "price")
    private double price;

    @ManyToOne
    @JoinColumn(name = "event_id" , nullable = false)
    private Event event;

    @OneToMany(mappedBy = "session" , cascade = CascadeType.ALL)
    private List<Offer> Offers;

}
