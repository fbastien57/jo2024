package com.jo._4.controller;

import com.jo._4.entity.Cart;
import com.jo._4.entity.Order;
import com.jo._4.service.CartService;
import com.jo._4.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/all/payment")
public class PaymentController {

    private final CartService cartService;
    private final OrderService orderService;

    /** 🧾 Afficher la page de paiement */
    @GetMapping
    public String showPaymentPage(Model model, HttpSession session) {
        Cart cart = cartService.getCurrentCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("total", cart.getTotalPrice());
        return "AllAccess/payment/checkout"; // → ta page HTML de paiement
    }

    /** 💳 Valider le paiement */
    @PostMapping("/confirm")
    public String confirmPayment(HttpSession session, Model model) {
        Cart cart = cartService.getCurrentCart(session);

        // créer la commande + paiement
        Order order = orderService.createOrderFromCart(cart);

        cart.getItems().clear();

        model.addAttribute("order", order);
        model.addAttribute("payment", order.getPayment());
        return "AllAccess/order/paymentConfirmed"; // → page de confirmation
    }
}