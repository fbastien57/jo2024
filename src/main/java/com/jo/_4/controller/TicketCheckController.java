package com.jo._4.controller;

import com.jo._4.entity.Ticket;
import com.jo._4.entity.TicketState;
import com.jo._4.repository.TicketRepository;
import com.jo._4.Config.TicketCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/check")
public class TicketCheckController {

    private final TicketRepository ticketRepository;

    @GetMapping("/verify")
    public ResponseEntity<?> verifyTicket(@RequestParam String qrData) {
        try {
            // 🔑 Hash du code scanné
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(qrData.getBytes(StandardCharsets.UTF_8));
            String hashedCode = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

            // 🔍 Recherche en base
            Ticket ticket = ticketRepository.findByCode(hashedCode)
                    .orElse(null);

            if (ticket == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "status", "INVALID",
                        "message", "❌ Billet invalide !"
                ));
            }

            if (ticket.getState() == TicketState.USED) {
                return ResponseEntity.ok(Map.of(
                        "status", "ALREADY_USED",
                        "message", "⚠️ Ce billet a déjà été utilisé !",
                        "ticketId", ticket.getId()
                ));
            }

            // ✅ Marquer comme utilisé
            ticket.setState(TicketState.USED);
            ticketRepository.save(ticket);

            return ResponseEntity.ok(Map.of(
                    "status", "VALID",
                    "message", "✅ Billet valide !",
                    "ticketId", ticket.getId(),
                    "user", ticket.getOrderItem().getOrder().getUser().getUsername(),
                    "session", ticket.getOrderItem().getSession().getName()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "ERROR",
                    "message", "Erreur lors de la vérification du billet"
            ));
        }
    }

    /** ✅ Page du contrôle des billets */
    @GetMapping("/control")
    public String TicketControl() {
        return "control/control";
    }
}
