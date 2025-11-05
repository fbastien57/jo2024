package com.jo._4.service;

import com.jo._4.entity.Cart;
import com.jo._4.entity.CartItem;
import com.jo._4.entity.Session;
import com.jo._4.entity.User;
import com.jo._4.repository.CartRepository;
import com.jo._4.repository.SessionRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final SessionRepository sessionRepository;
    private  final UserService userService;

    public Cart getCurrentCart(HttpSession httpSession) {
        User currentUser = userService.getAuthenticatedUser();
        if (currentUser != null) {
            return cartRepository.findByUser(currentUser)
                    .orElseGet(() -> createCartForUser(currentUser));
        } else {
            String sessionId = httpSession.getId();
            return cartRepository.findBySessionId(sessionId)
                    .orElseGet(() -> createCartForSession(sessionId));
        }
    }

    private Cart createCartForUser(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCreatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    private Cart createCartForSession(String sessionId) {
        Cart cart = new Cart();
        cart.setSessionId(sessionId);
        cart.setCreatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    public void addToCart(Long sessionId, int quantity, HttpSession httpSession) {
        Cart cart = getCurrentCart(httpSession);

        // 🔒 Récupération verrouillée pour éviter la concurrence
        Session session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée"));

        if (session.getRemainingCapacity() < quantity) {
            throw new IllegalStateException("Pas assez de places disponibles pour cet événement.");
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getSession().getId().equals(sessionId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            if (session.getRemainingCapacity() < quantity) {
                throw new IllegalStateException("Stock insuffisant.");
            }

            item.setQuantity(newQuantity);
            session.setRemainingCapacity(session.getRemainingCapacity() - quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setSession(session);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
            session.setRemainingCapacity(session.getRemainingCapacity() - quantity);
        }

        sessionRepository.save(session);
        cartRepository.save(cart);
    }

    /** ✅ Supprimer un article du panier */
    public void removeItem(Long sessionId, HttpSession httpSession) {
        Cart cart = getCurrentCart(httpSession);

        cart.getItems().stream()
                .filter(item -> item.getSession().getId().equals(sessionId))
                .findFirst()
                .ifPresent(item -> {
                    // 🔒 Récupération verrouillée
                    Session session = sessionRepository.findByIdForUpdate(sessionId)
                            .orElseThrow(() -> new RuntimeException("Session non trouvée"));

                    session.setRemainingCapacity(session.getRemainingCapacity() + item.getQuantity());
                    sessionRepository.save(session);

                    cart.getItems().remove(item);
                    cartRepository.save(cart);
                });
    }

    /** ✅ Modifier la quantité d’un article */
    public void updateItemQuantity(Long sessionId, int newQuantity, HttpSession httpSession) {
        Cart cart = getCurrentCart(httpSession);

        cart.getItems().stream()
                .filter(item -> item.getSession().getId().equals(sessionId))
                .findFirst()
                .ifPresent(item -> {
                    // 🔒 Récupération verrouillée
                    Session session = sessionRepository.findByIdForUpdate(sessionId)
                            .orElseThrow(() -> new RuntimeException("Session non trouvée"));

                    int currentQuantity = item.getQuantity();
                    int difference = newQuantity - currentQuantity;

                    if (difference > 0) {
                        if (session.getRemainingCapacity() < difference) {
                            throw new IllegalStateException("Pas assez de places disponibles.");
                        }
                        session.setRemainingCapacity(session.getRemainingCapacity() - difference);
                    } else if (difference < 0) {
                        session.setRemainingCapacity(session.getRemainingCapacity() + Math.abs(difference));
                    }

                    item.setQuantity(newQuantity);
                    sessionRepository.save(session);
                    cartRepository.save(cart);
                });
    }
}
