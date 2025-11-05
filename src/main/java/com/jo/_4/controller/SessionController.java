package com.jo._4.controller;

import com.jo._4.entity.Event;
import com.jo._4.entity.Offer;
import com.jo._4.entity.Session;
import com.jo._4.repository.EventRepository;
import com.jo._4.repository.OfferRepository;
import com.jo._4.repository.SessionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SessionController {


    private final SessionRepository sessionRepository;

    private final EventRepository eventRepository;

    private final OfferRepository offerRepository;

    @GetMapping("/event/{eventId}/sessions/new")
    public String newSession(@PathVariable("eventId") Long eventId, Model model) {
        model.addAttribute("eventSession", new Session());
        model.addAttribute("eventId", eventId);
        return "session/newSession";
    }

    @PostMapping("/session/create")
    public String createSession(
            @RequestParam("eventId") Long eventId,
            @Valid @ModelAttribute("eventSession") Session session,
            BindingResult result
    ){
        if (result.hasErrors()) {
            return "session/newSession";
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        session.setEvent(event);

        sessionRepository.save(session);
        return "redirect:/event/" + event.getId();
    }

    @GetMapping("/event/{eventId}/session/edit/{id}")
    public String editSession(@PathVariable("id") Long id,@PathVariable("eventId") Long eventId, Model model) {

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        model.addAttribute("eventId", eventId);
        Session session = sessionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Session not found: " + id));
        model.addAttribute("eventSession", session);

        return "session/editSession";
    }

    @PostMapping("/session/update/{id}")
    public String updateSession(@PathVariable("id") Long id,@RequestParam("eventId") Long eventId, @Valid @ModelAttribute("session") Session session,BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "session/editSession";
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        session.setEvent(event);

        session.setEvent(event);
        session.setId(id);

        sessionRepository.save(session);
        return "redirect:/event/" + event.getId();
    }

    @GetMapping("session/delete/{id}")
    public String deleteSession(@PathVariable("id") Long id, Model model) {
        Session session = sessionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Session not found: " + id));
        sessionRepository.delete(session);
        return "redirect:/event/" + session.getEvent().getId();
    }

    @GetMapping("/event/{eventId}/session/{sessionId}")
    public String sessionDetail(@PathVariable("eventId") Long eventId,@PathVariable("sessionId") Long sessionId,Model model) {
        Event event = eventRepository.findById(eventId).orElseThrow(()-> new IllegalArgumentException("Event not found"));
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!session.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Session does not belong to the given event");
        }

        List<Offer> offers = offerRepository.findBySessionId(sessionId);
        model.addAttribute("event", event);
        model.addAttribute("sess", session);
        model.addAttribute("offers", offers);
        return "session/sessionDetail";
    }

    @GetMapping("/all/event/{eventId}/session/{sessionId}")
    public String sessionBooking(@PathVariable("eventId") Long eventId,@PathVariable("sessionId") Long sessionId,Model model) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!session.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Session does not belong to the given event");
        }
        List<Offer> offers = offerRepository.findBySessionId(sessionId);
        model.addAttribute("event", event);
        model.addAttribute("sess", session);
        model.addAttribute("offers", offers);
        return "AllAccess/ASessionDetail";
    }
}
