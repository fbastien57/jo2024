package com.jo._4.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "ticket")
public class Ticket {

    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Column(name = "pdf_path")
    private String pdfPath;

    @Column(name = "qr_code_path")
    private String qrCodePath;

    /* Code unique pour le QR code */
    @Column(unique = true, nullable = false, name = "code")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private TicketState state = TicketState.NOT_USED; // valeur par défaut

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(nullable = false, unique = true, updatable = false , name = "security_key")
    private UUID securityKey;

    @PrePersist
    public void prePersist() {
        if (securityKey == null) {
            securityKey = UUID.randomUUID();
        }
        if (state == null) {
            state = TicketState.NOT_USED;
        }
    }
}
