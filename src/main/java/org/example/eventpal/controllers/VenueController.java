package org.example.eventpal.controllers;

import jakarta.persistence.GeneratedValue;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventpal.dto.event.EventResponse;
import org.example.eventpal.dto.venue.CreateVenueRequest;
import org.example.eventpal.dto.venue.GetAllVenueResponse;
import org.example.eventpal.dto.venue.VenueSummary;
import org.example.eventpal.services.VenueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {
    private final VenueService venueService;

    @GetMapping
    public ResponseEntity<GetAllVenueResponse> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }
    @GetMapping("/{id}")
    public ResponseEntity<VenueSummary> getVenueById(@PathVariable long id) {
        return ResponseEntity.ok(venueService.getVenueById(id));
    }
    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueSummary> createVenue(@Valid @RequestBody CreateVenueRequest request) {
        return ResponseEntity.ok(venueService.createVenue(request));
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueSummary> updateVenue(@PathVariable Long id,
                                                      @Valid @RequestBody CreateVenueRequest request) {
        return ResponseEntity.ok(venueService.updateVenue(id, request));
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping(value = "/{venueId}/events", produces = "application/json")
    public Page<EventResponse> listEventsForVenue(
            @PathVariable Long venueId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) LocalDateTime startsAtFrom,
            @RequestParam(required = false) LocalDateTime startsAtTo,
            @PageableDefault(size = 20, sort = "startsAt") Pageable pageable
    ) {
        return venueService.listEventsForVenue(venueId, q, startsAtFrom, startsAtTo, pageable);
    }
}
