package com.jo._4.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "user")
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotEmpty(message = "The email can't be empty")
    @Column(name = "username")
    private String username;

    @Length(min = 8 , message = "Le mot de passe doit être de 8 caractères minimum")
    @NotEmpty(message = "The password can't be empty")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$",
            message = "Le mot de passe doit contenir au moins une majuscule et un caractère spécial"
    )
    @Column(name = "password")
    private String password;

    @NotEmpty(message = "The name can't be empty")
    @Column(name = "name")
    private String name;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user" , cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;

    @Column(nullable = false , unique = true, updatable = false , name = "security_key")
    private UUID securityKey;

   @PrePersist
   public void prePersist() {
       if (securityKey == null) {
          securityKey = UUID.randomUUID();
       }
    }

}
