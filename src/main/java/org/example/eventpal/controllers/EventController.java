package org.example.eventpal.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventpal.dto.event.*;
import org.example.eventpal.services.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    @PostMapping
    @PreAuthorize("hasRole('ORGANISER')")
    public ResponseEntity<EventDetailsResponse> create(@Valid @RequestBody CreateEventRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getEventById(id));
    }

    @GetMapping
    public Page<EventResponse> search(
            @Valid SearchEventRequest req,
            @PageableDefault(size = 20, sort = "startsAt") Pageable pageable
    ) {
        return service.search(req, pageable);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANISER')")
    public EventDetailsResponse update(@PathVariable Long id, @Valid @RequestBody UpdateEventRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ORGANISER')")
    public EventDetailsResponse partialUpdate(@PathVariable Long id, @RequestBody UpdateEventRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ORGANISER')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
