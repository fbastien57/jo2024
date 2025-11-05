package com.jo._4.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.LongSummaryStatistics;

@Getter
@Setter
@Entity
@Table(name = "offer")
public class Offer {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "The name is required")
    @Column(name = "name")
    private String name;

    @NotNull(message = "The number can't be null ")
    @Column(name = "nbr_billet")
    private int nbrBillet;

    @ManyToOne
    @JoinColumn(name = "session_id" , nullable = false)
    private Session session;


}
