package com.jo._4.controller;

import com.jo._4.Config.UserDetailsImpl;
import com.jo._4.entity.Order;
import com.jo._4.entity.User;
import com.jo._4.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/all/orders")
public class UserOrderController {

    private final OrderService orderService;

    @GetMapping
    public String getUserOrders(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();

        List<Order> orders = orderService.findOrdersByUser(user);
        model.addAttribute("orders", orders);

        return "/AllAccess/userOrder/userOrder";
    }
}