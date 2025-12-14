package org.example.eventpal.services;

import lombok.RequiredArgsConstructor;
import org.example.eventpal.dto.event.EventResponse;
import org.example.eventpal.dto.venue.CreateVenueRequest;
import org.example.eventpal.dto.venue.GetAllVenueResponse;
import org.example.eventpal.dto.venue.VenueSummary;
import org.example.eventpal.entities.Event;
import org.example.eventpal.entities.Venue;
import org.example.eventpal.exceptions.VenueAlreadyExistsException;
import org.example.eventpal.exceptions.VenueNotFoundException;
import org.example.eventpal.mappers.VenueMapper;
import org.example.eventpal.repositories.EventRepository;
import org.example.eventpal.repositories.VenueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;


    public GetAllVenueResponse getAllVenues() {
        GetAllVenueResponse response = new GetAllVenueResponse();
        response.setAllVenues(venueRepository.findAll().stream().map(VenueMapper::getVenueSummary).toList());
        return response;
    }
    public VenueSummary getVenueById(Long venueId){
        Optional<Venue> venue = venueRepository.findById(venueId);

        if(venue.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found");
        return VenueMapper.getVenueSummary(venue.get());
    }

    public VenueSummary createVenue(CreateVenueRequest request) {

        Venue venue =
                Venue.builder()
                        .name(request.getName())
                        .address(request.getAddress())
                        .seatCount(request.getSeatCount())
                        .build();
        if(venueRepository.existsByName(request.getName())){
            throw new VenueAlreadyExistsException("Venue already exists");
        }
        Venue savedVenue = venueRepository.save(venue);

        return VenueMapper.getVenueSummary(savedVenue);
    }
    public VenueSummary updateVenue(Long id, CreateVenueRequest request) {
        Venue venue =
                venueRepository
                        .findById(id)
                        .orElseThrow(() -> new VenueNotFoundException("Venue not found"));

        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setSeatCount(request.getSeatCount());

        Venue updatedVenue = venueRepository.save(venue);

        return VenueMapper.getVenueSummary(updatedVenue);
    }

    public void deleteVenue(Long id) {
        if (!venueRepository.existsById(id)) throw new VenueNotFoundException("Venue does not exist");
        long inUse = eventRepository.countByVenue_Id(id);
        if (inUse > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete venue; "+inUse+" event(s) reference it");
        }
        venueRepository.deleteById(id);
    }

    public Page<EventResponse> listEventsForVenue(
            Long venueId,
            String q,
            LocalDateTime startsAtFrom,
            LocalDateTime startsAtTo,
            Pageable pageable
    ) {
        // Validate parent resource here (service owns the rule)
        if (!venueRepository.existsById(venueId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found: " + venueId);
        }

        // Delegate to repository once, keep mapping here
        return eventRepository.search(
                emptyToNull(q),
                venueId,
                startsAtFrom,
                startsAtTo,
                pageable
        ).map(this::mapToSummary);
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
    private EventResponse mapToSummary(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .startsAt(event.getStartsAt())
                .venue(org.example.eventpal.dto.venue.VenueSummary.builder()
                        .id(event.getVenue().getId())
                        .venueName(event.getVenue().getName())
                        .venueAddress(event.getVenue().getAddress())// ensure your DTO field is 'name'
                        .build())
                .description(event.getDescription())
                .build();
    }
}
