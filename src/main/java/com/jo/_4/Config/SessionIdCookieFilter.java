package com.jo._4.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SessionIdCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Appliquer uniquement si ce n’est pas une requête POST (soumission du login)
        if ("/login".equals(request.getRequestURI()) && "GET".equalsIgnoreCase(request.getMethod())) {
            var session = request.getSession(false); // ne crée pas de session
            if (session != null) {
                String sessionId = session.getId();
                Cookie oldSessionCookie = new Cookie("OLD_SESSION_ID", sessionId);
                oldSessionCookie.setPath("/");
                oldSessionCookie.setHttpOnly(true);
                oldSessionCookie.setMaxAge(60 * 10); // expire dans 10 minutes (optionnel)
                response.addCookie(oldSessionCookie);

            } else {
                System.out.println("⚠️ Aucune session trouvée sur /login GET");
            }
        }

        filterChain.doFilter(request, response);
    }
}