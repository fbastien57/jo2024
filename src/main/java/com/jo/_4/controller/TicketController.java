package com.jo._4.controller;

import com.jo._4.Config.UserDetailsImpl;
import com.jo._4.entity.Ticket;
import com.jo._4.entity.User;
import com.jo._4.repository.TicketRepository;
import com.jo._4.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequiredArgsConstructor
@RequestMapping("/all/tickets")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/ticket/{id}")
    public String getTicket(@PathVariable Long id, Model model) {
        // Récupération du ticket...
        Ticket ticket = ticketService.findById(id);

        String qrFile = "/uploads/tickets/qr/" + ticket.getSecurityKey() + ".png";
        String pdfFile = "/uploads/tickets/" + ticket.getSecurityKey() + ".pdf";

        model.addAttribute("ticket", ticket);
        model.addAttribute("qrFileName", qrFile);
        model.addAttribute("pdfFileName", pdfFile);

        return "/AllAccess/userOrder/ticketView";
    }
}
