package com.jo._4.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.SessionEvent;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotEmpty(message = "The name is required")
    @Size(min = 3 , message = "The name should be at least 3 characters")
    @Column(name = "name")
    private String name;

    @NotEmpty(message = "The description is required")
    @Size(min = 10 , message = "The description should be at least 10 characters")
    @Column(name = "description")
    private String description;

    @NotEmpty(message = "The location is required")
    @Size(min = 3 , message = "The location should be at least 3 characters")
    @Column(name = "location")
    private String location;

    @Column(name = "picture")
    private String picture;

    @OneToMany(mappedBy = "event" , cascade = CascadeType.ALL)
    private List<Session> sessions;

}
