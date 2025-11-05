package com.jo._4.controller;

import com.jo._4.entity.Event;
import com.jo._4.entity.Session;
import com.jo._4.repository.EventRepository;
import com.jo._4.repository.SessionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;

    private final SessionRepository sessionRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("events", eventRepository.findAll());
        return "index";
    }

    @GetMapping("/event")
    public String eventList(Model model){
        model.addAttribute("events", eventRepository.findAll());
        return "event/event";
    }

    @GetMapping("/event/new")
    public String newEvent(Model model) {
        model.addAttribute("event", new Event());
        return "event/newEvent";
    }

    @Value("${upload.dir}")
    private String uploadDir;

    @PostMapping("/event/create")
    public String createEvent(@Valid @ModelAttribute("event") Event event,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "event/newEvent";
        }

        if (!imageFile.isEmpty()) {
            try {
                String originalFilename = imageFile.getOriginalFilename();

                // --- 1. Vérifier l'extension ---
                String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                if (!List.of("jpg", "jpeg", "png", "webp").contains(extension)) {
                    redirectAttributes.addFlashAttribute("error", "Format d'image non supporté !");
                    return "redirect:/event/new";
                }

                // --- 2. Vérifier la taille brute (max 5 Mo pour éviter MaxUploadSizeExceeded) ---
                if (imageFile.getSize() > 5 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "Image trop volumineuse (max 5 Mo) !");
                    return "redirect:/event/new";
                }

                // --- 3. Redimensionner l'image ---
                BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());

                int maxWidth = 800;  // largeur max
                int maxHeight = 600; // hauteur max

                BufferedImage resizedImage = Thumbnails.of(originalImage)
                        .size(maxWidth, maxHeight)
                        .keepAspectRatio(true)   // conserve le ratio
                        .asBufferedImage();

                // --- 4. Enregistrer l'image sur le serveur ---
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
                Path filePath = uploadPath.resolve(uniqueFilename);

                // Pour JPG, on écrit "jpeg" sinon on prend l'extension telle quelle
                ImageIO.write(resizedImage, extension.equals("jpg") ? "jpeg" : extension, filePath.toFile());

                event.setPicture(uniqueFilename);

            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Erreur lors de l'upload de l'image !");
                return "redirect:/event/new";
            }
        }

        eventRepository.save(event);
        return "redirect:/event";
    }



    @GetMapping("/event/edit/{id}")
    public String editEvent(@PathVariable("id") Long id , Model model) {
        Event event = eventRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Event not found"));
        model.addAttribute("event", event);
        return "event/editEvent";
    }

    @GetMapping("/event/{id}")
    public String eventDetail(@PathVariable("id") Long id, Model model) {
        Event event = eventRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Event not found"));
        List<Session> sessions = sessionRepository.findByEventId(id);
        model.addAttribute("event", event);
        model.addAttribute("sessions", sessions);
        return "event/eventDetail";
    }

    @GetMapping("/all/event/{id}")
    public String ASessionList(@PathVariable("id") Long id, Model model) {
        Event event = eventRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Event not found"));
        List<Session> sessions = sessionRepository.findByEventId(id);
        model.addAttribute("event", event);
        model.addAttribute("sessions", sessions);
        return "AllAccess/ASession";
    }


    @PostMapping("/event/{id}")
    public String updateEvent(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("event") Event event,
                              BindingResult result,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "event/editEvent";
        }

        Event existingEvent = eventRepository.findById(id).orElse(null);
        if (existingEvent == null) {
            redirectAttributes.addFlashAttribute("error", "Événement introuvable !");
            return "redirect:/event";
        }

        if (!imageFile.isEmpty()) {
            try {
                String originalFilename = imageFile.getOriginalFilename();

                // --- 1. Vérifier l'extension ---
                String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                if (!List.of("jpg", "jpeg", "png", "webp").contains(extension)) {
                    redirectAttributes.addFlashAttribute("error", "Format d'image non supporté !");
                    return "redirect:/event/edit/" + id;
                }

                // --- 2. Vérifier la taille brute (max 5 Mo) ---
                if (imageFile.getSize() > 5 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "Image trop volumineuse (max 5 Mo) !");
                    return "redirect:/event/edit/" + id;
                }

                // --- 3. Redimensionner l'image ---
                BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());
                int maxWidth = 800;
                int maxHeight = 600;
                BufferedImage resizedImage = Thumbnails.of(originalImage)
                        .size(maxWidth, maxHeight)
                        .keepAspectRatio(true)
                        .asBufferedImage();

                // --- 4. Enregistrer la nouvelle image ---
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
                Path filePath = uploadPath.resolve(uniqueFilename);
                ImageIO.write(resizedImage, extension.equals("jpg") ? "jpeg" : extension, filePath.toFile());

                // --- 5. Supprimer l'ancienne image ---
                if (existingEvent.getPicture() != null) {
                    Path oldFilePath = uploadPath.resolve(existingEvent.getPicture());
                    Files.deleteIfExists(oldFilePath);
                }

                event.setPicture(uniqueFilename);

            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Erreur lors de l'upload de l'image !");
                return "redirect:/event/edit/" + id;
            }
        } else {
            // Si aucune nouvelle image, conserver l'ancienne
            event.setPicture(existingEvent.getPicture());
        }

        event.setId(id);
        eventRepository.save(event);
        return "redirect:/event";
    }


    @GetMapping("/event/delete/{id}")
    public String deleteEvent(@PathVariable("id") Long id, Model model) {
        Event event = eventRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Event not found"));

        if (event.getPicture() != null) {
            Path uploadPath = Paths.get(uploadDir);
            Path imagePath = uploadPath.resolve(event.getPicture());
            try {
                Files.deleteIfExists(imagePath);

            }catch (IOException e){
                e.printStackTrace();
            }
        }

        eventRepository.delete(event);
        return "redirect:/event";
    }
}
