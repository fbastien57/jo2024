package com.jo._4.controller;

import com.jo._4.entity.Cart;
import com.jo._4.entity.CartItem;
import com.jo._4.entity.Offer;
import com.jo._4.repository.OfferRepository;
import com.jo._4.repository.SessionRepository;
import com.jo._4.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/all/cart")
public class CartController {

    private final CartService cartService;
    private final SessionRepository sessionRepository;
    private final OfferRepository offerRepository;

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        Cart cart = cartService.getCurrentCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("total", cart.getTotalPrice());
        return "AllAccess/cart/viewCart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long sessionId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session) {
        cartService.addToCart(sessionId, quantity, session);
        return "redirect:/all/cart";
    }

    @DeleteMapping("/remove/{sessionId}")
    @ResponseBody
    public ResponseEntity<?> removeItem(@PathVariable Long sessionId, HttpSession session) {
        cartService.removeItem(sessionId, session);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{sessionId}")
    @ResponseBody
    public ResponseEntity<?> updateQuantity(@PathVariable Long sessionId,
                                            @RequestBody java.util.Map<String, Integer> body,
                                            HttpSession session) {
        int newQuantity = body.get("quantity");
        cartService.updateItemQuantity(sessionId, newQuantity, session);

        Cart cart = cartService.getCurrentCart(session);
        CartItem updatedItem = cart.getItems().stream()
                .filter(item -> item.getSession().getId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

        double lineTotal = updatedItem.getSession().getPrice() * updatedItem.getQuantity();
        double cartTotal = cart.getTotalPrice();

        System.out.println("UpdateQuantity called for sessionId=" + sessionId + ", newQuantity=" + newQuantity);
        System.out.println("lineTotal = " + lineTotal + ", cartTotal = " + cartTotal);


        // On renvoie un JSON avec les totaux
        return ResponseEntity.ok().body(Map.of(
                "lineTotal", lineTotal,
                "cartTotal", cartTotal
        ));
    }

    @PostMapping("/addOffer/{offerId}")
    public String addOfferToCart(@PathVariable Long offerId, HttpSession session) {
        // Récupération de l'offre
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offre non trouvée: " + offerId));

        Long sessionId = offer.getSession().getId();
        int quantity = offer.getNbrBillet();

        // Ajout au panier
        cartService.addToCart(sessionId, quantity, session);

        // Redirection vers le panier
        return "redirect:/all/cart";
    }

}
