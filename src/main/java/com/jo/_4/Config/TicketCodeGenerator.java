package com.jo._4.Config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public class TicketCodeGenerator {

    public static String generate(UUID userKey, UUID paymentKey, Long itemId) {
        try {
            String input = userKey.toString() + ":" + paymentKey.toString() + ":" + itemId;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Encodage en base64 URL-safe (sans caractères spéciaux)
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

            // On garde 20 caractères pour un code compact
            return "TKT-" + encoded.substring(0, 20).toUpperCase();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur génération code ticket", e);
        }
    }
}
