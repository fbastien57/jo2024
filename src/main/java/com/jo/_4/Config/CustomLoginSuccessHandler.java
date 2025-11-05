package com.jo._4.Config;

import com.jo._4.entity.Cart;
import com.jo._4.entity.CartItem;
import com.jo._4.entity.User;
import com.jo._4.repository.CartRepository;
import com.jo._4.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {


        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null) {
            String sessionId = request.getSession().getId();

            // ✅ Lire l'ancien sessionId depuis le cookie
            String oldSessionId = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("OLD_SESSION_ID".equals(cookie.getName())) {
                        oldSessionId = cookie.getValue();
                        break;
                    }
                }
            }


            Optional<Cart> anonymousCartOpt = Optional.empty();
            if (oldSessionId != null) {
                anonymousCartOpt = cartRepository.findBySessionId(oldSessionId);
            }


            Optional<Cart> userCartOpt = cartRepository.findByUser(user);

            if (anonymousCartOpt.isPresent()) {
                Cart anonymousCart = anonymousCartOpt.get();
                System.out.println("🛒 Panier anonyme avec " + anonymousCart.getItems().size() + " articles.");

                if (userCartOpt.isPresent()) {
                    Cart userCart = userCartOpt.get();
                    System.out.println("🛍️ Panier utilisateur existant avec " + userCart.getItems().size() + " articles.");

                    for (CartItem item : anonymousCart.getItems()) {
                        Optional<CartItem> existingItem = userCart.getItems().stream()
                                .filter(i -> i.getSession().getId().equals(item.getSession().getId()))
                                .findFirst();

                        if (existingItem.isPresent()) {
                            existingItem.get().setQuantity(
                                    existingItem.get().getQuantity() + item.getQuantity());
                        } else {
                            CartItem newItem = new CartItem();
                            newItem.setCart(userCart);
                            newItem.setSession(item.getSession());
                            newItem.setQuantity(item.getQuantity());
                            userCart.getItems().add(newItem);
                        }
                    }

                    cartRepository.save(userCart);
                    cartRepository.delete(anonymousCart);

                } else {
                    anonymousCart.setUser(user);
                    anonymousCart.setSessionId(null);
                    cartRepository.save(anonymousCart);
                }
            } else {
                System.out.println("⚠️ Aucun panier anonyme trouvé.");
            }

            // ✅ Supprimer le cookie OLD_SESSION_ID
            Cookie deleteCookie = new Cookie("OLD_SESSION_ID", "");
            deleteCookie.setPath("/");
            deleteCookie.setMaxAge(0);
            response.addCookie(deleteCookie);
        }

        // Rediriger après login
        response.sendRedirect("/"); // ou vers "/cart"
    }
}