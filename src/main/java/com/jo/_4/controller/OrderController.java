package com.jo._4.controller;

import com.jo._4.entity.Cart;
import com.jo._4.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/all/order")
public class OrderController {

    private final CartService cartService;

    @GetMapping
    public String viewOrder(Model model, HttpSession session) {
        Cart cart = cartService.getCurrentCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("total", cart.getTotalPrice());
        return "AllAccess/order/viewOrder";
    }

}

