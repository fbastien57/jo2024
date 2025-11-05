package com.jo._4.controller;

import com.jo._4.entity.Event;
import com.jo._4.entity.Offer;
import com.jo._4.entity.Session;
import com.jo._4.repository.EventRepository;
import com.jo._4.repository.OfferRepository;
import com.jo._4.repository.SessionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class OfferController {

    private final OfferRepository offerRepository;
    private final SessionRepository sessionRepository;
    private final EventRepository eventRepository;

    @GetMapping("/event/{eventId}/session/{sessionId}/offer/new")
    public String newOffer(@PathVariable("eventId") Long eventId,@PathVariable("sessionId") Long sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("eventId", eventId);
        model.addAttribute("offer", new Offer());
        return "Offer/newOffer";
    }

    @PostMapping("/offer/create")
    public String createSession(
            @RequestParam("eventId") Long eventId,
            @RequestParam("sessionId") Long sessionId,
            @Valid @ModelAttribute("offer") Offer offer,
            BindingResult result
    ){
        if (result.hasErrors()) {
            return "Offer/newOffer";
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        offer.setSession(session);
        offerRepository.save(offer);
        return "redirect:/event/" + eventId + "/session/" + sessionId;
    }

    @GetMapping("/event/{eventId}/session/{sessionId}/offer/edit/{id}")
    public String editOffer(@PathVariable("eventId") Long eventId ,@PathVariable("sessionId") Long sessionId,@PathVariable("id") Long id , Model model) {

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        model.addAttribute("eventId", eventId);
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        model.addAttribute("sess", session);
        Offer offer = offerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Offer not found: " + id));
        model.addAttribute("offer", offer);

        return "Offer/editOffer";
    }

    @PostMapping("/offer/update/{id}")
    public String updateOffer(@PathVariable("id") Long id,@RequestParam("eventId") Long eventId,@RequestParam("sessionId") Long sessionId,@Valid @ModelAttribute("offer") Offer offer,BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "Offer/editOffer";
        }

        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        offer.setSession(session);
        offer.setId(id);
        offerRepository.save(offer);

        return "redirect:/event/" + eventId + "/session/" + sessionId;
    }

    @GetMapping("/event/{eventId}/session/{sessionId}/offer/delete/{id}")
    public String deleteOffer(@PathVariable("id") Long id,@PathVariable("eventId") Long eventId , @PathVariable("sessionId") Long sessionId,Model model) {

            Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
            Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
            Offer offer = offerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Offer not found: " + id));
            offerRepository.delete(offer);
        return "redirect:/event/" + eventId + "/session/" + sessionId;
    }
}
