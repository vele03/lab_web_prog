package mk.finki.ukim.mk.lab.web.controller;

import mk.finki.ukim.mk.lab.model.Event;
import mk.finki.ukim.mk.lab.service.EventService;
import mk.finki.ukim.mk.lab.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequestMapping("/events")
@Controller
public class EventController {
    private final EventService eventService;
    private final LocationService locationService;

    @Autowired
    public EventController(EventService eventService, LocationService locationService) {
        this.eventService = eventService;
        this.locationService = locationService;
    }
    @GetMapping
    public String getEventsPage(@RequestParam(required = false) String searchText,
                                @RequestParam(required = false) Double minRating,
                                @RequestParam(required = false) Long locationId,
                                @RequestParam(required = false) Error error,
                                Model model, Authentication authentication) {

        List<Event> events = eventService.searchEvents(searchText, minRating, locationId);
        model.addAttribute("events", events);
        model.addAttribute("locations", locationService.findAll());
        model.addAttribute("error", error);

        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("roles", authentication.getAuthorities());
            model.addAttribute("isAdmin", authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
        }

        return "listEvents";
    }
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveEvent(@RequestParam String name,
                            @RequestParam String description,
                            @RequestParam double popularityScore,
                            @RequestParam Long locationId) {
        eventService.save(name, description, popularityScore, locationId);
        return "redirect:/events";
    }
    @GetMapping("/edit/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editEvent(@PathVariable Long eventId, Model model) {
        System.out.println("Editing event with ID: " + eventId);
        Optional<Event> event = eventService.findById(eventId);
        if (event.isEmpty()) {
            return "redirect:/events?error=EventNotFound";
        }
        model.addAttribute("event", event.get());
        model.addAttribute("locations", locationService.findAll());
        return "add-event";
    }
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteEvent(@PathVariable Long id) {
        eventService.delete(id);
        return "redirect:/events";
    }
    @PostMapping("/edit/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateEvent(@PathVariable Long eventId,
                              @RequestParam String name,
                              @RequestParam String description,
                              @RequestParam double popularityScore,
                              @RequestParam Long locationId) {

        eventService.update(eventId, name, description, popularityScore, locationId);
        return "redirect:/events";
    }
    @GetMapping("/edit-form/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String getEditEventForm(@PathVariable Long id, Model model) {
        Optional<Event> event = eventService.findById(id);
        if (event.isEmpty()) {
            return "redirect:/events?error=EventNotFound";
        }
        model.addAttribute("event", event.get());
        model.addAttribute("locations", locationService.findAll());
        return "add-event";
    }

    @GetMapping("/add-form")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAddEventPage(Model model) {
        model.addAttribute("locations", locationService.findAll());
        return "add-event1";
    }



}
