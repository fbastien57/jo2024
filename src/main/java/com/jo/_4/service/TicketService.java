package com.jo._4.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.jo._4.Config.TicketCodeGenerator;
import com.jo._4.entity.OrderItem;
import com.jo._4.entity.Payment;
import com.jo._4.entity.Ticket;
import com.jo._4.entity.User;
import com.jo._4.repository.TicketRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final SpringTemplateEngine templateEngine;

    private static final String UPLOAD_DIR = "uploads/tickets/";

    public Ticket generateTicket(OrderItem orderItem, int index) throws Exception {
        User user = orderItem.getOrder().getUser();
        Payment payment = orderItem.getOrder().getPayment();

        long uniqueId = (orderItem.getId() != null ? orderItem.getId() : System.currentTimeMillis()) + index;

        // 1️⃣ Génération du code original
        String code = TicketCodeGenerator.generate(user.getSecurityKey(), payment.getSecurityKey(), uniqueId);

        // 2️⃣ Génération du hash à stocker
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(code.getBytes());
        String hashedCode = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

        // 3️⃣ Création initiale du ticket pour obtenir son UUID
        Ticket ticket = new Ticket();
        ticket.setOrderItem(orderItem);
        ticket.setCode(hashedCode);
        ticket.setGeneratedAt(LocalDateTime.now());

        // Le UUID est créé automatiquement par @PrePersist, donc on sauvegarde une première fois
        ticket = ticketRepository.save(ticket);

        // 4️⃣ Utilisation du UUID comme nom de fichier
        String ticketId = ticket.getSecurityKey().toString();

        String qrPath = UPLOAD_DIR + "qr/" + ticketId + ".png";
        generateQRCodeImage(code, 250, 250, qrPath);

        String pdfPath = UPLOAD_DIR + ticketId + ".pdf";
        generateTicketPDF(orderItem, code, qrPath, pdfPath);

        // 5️⃣ Mise à jour des chemins puis sauvegarde finale
        ticket.setQrCodePath(qrPath);
        ticket.setPdfPath(pdfPath);

        return ticketRepository.save(ticket);
    }


    private void generateQRCodeImage(String text, int width, int height, String filePath) throws Exception {
        BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height);
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        MatrixToImageWriter.writeToPath(matrix, "PNG", Path.of(filePath));
    }

    private void generateTicketPDF(OrderItem orderItem, String code, String qrPath, String pdfPath) throws Exception {
        Context context = new Context();
        context.setVariable("orderItem", orderItem);
        context.setVariable("code", code);
        context.setVariable("qrPath", qrPath);

        String html = templateEngine.process("AllAccess/ticket/template", context);

        File pdfFile = new File(pdfPath);
        pdfFile.getParentFile().mkdirs();

        try (FileOutputStream os = new FileOutputStream(pdfFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, new File(".").toURI().toString());
            builder.toStream(os);
            builder.run();
        }
    }

    public Ticket findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
    }
}