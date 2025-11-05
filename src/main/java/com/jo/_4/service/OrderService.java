package com.jo._4.service;

import com.jo._4.entity.User;
import com.jo._4.entity.*;
import com.jo._4.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    private TicketService ticketService;
    @Autowired
    private MailService mailService;

    /** ✅ Créer une commande et un paiement à partir d’un panier */
    public Order createOrderFromCart(Cart cart) {
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Le panier est vide !");
        }

        // 1️⃣ Créer la commande
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderDate(LocalDateTime.now());
        orderRepository.save(order);

        // 2️⃣ Transférer les articles du panier vers la commande
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setSession(cartItem.getSession());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);
        orderItemRepository.saveAll(orderItems);

        // 3️⃣ Créer le paiement associé
        double total = cart.getTotalPrice();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPrice(total);
        payment.setStatus(Status.ACCEPTED);
        payment.setPayedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 🧩 Liaison dans les deux sens
        order.setPayment(payment);
        orderRepository.save(order); // 💾 Sauvegarde la relation order <-> payment

        // ✅ Génération des tickets (avec index unique par billet)
        List<File> ticketFiles = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                try {
                    // ✅ on passe l’index pour éviter les doublons
                    Ticket ticket = ticketService.generateTicket(item, i);
                    ticketFiles.add(new File(ticket.getPdfPath()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // ✅ Envoi d’un seul mail avec tous les tickets en pièce jointe
        try {
            mailService.sendTicketsEmail(
                    order.getUser().getUsername(),
                    "Vos billets pour votre commande",
                    "<p>Merci pour votre achat ! Voici tous vos billets en pièce jointe.</p>",
                    ticketFiles
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 4️⃣ Vider le panier
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);

        return order;
    }

    public List<Order> findOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
}
