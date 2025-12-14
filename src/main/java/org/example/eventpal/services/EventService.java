package org.example.eventpal.services;

import jakarta.transaction.Transactional;
import org.example.eventpal.dto.event.*;
import org.example.eventpal.dto.ticketType.TicketTypeResponse;
import org.example.eventpal.dto.venue.VenueResponse;
import org.example.eventpal.entities.Event;
import org.example.eventpal.entities.Venue;
import org.example.eventpal.helpers.IdNameDTO;
import org.example.eventpal.helpers.MoneyDTO;
import org.example.eventpal.repositories.EventRepository;
import org.example.eventpal.repositories.TicketTypeRepository;
import org.example.eventpal.repositories.VenueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class EventService {
    private final EventRepository eventRepo;
    private final VenueRepository venueRepo;
    private final TicketTypeRepository ticketTypeRepo;

    public EventService(
            EventRepository eventRepo,
            VenueRepository venueRepo,
            TicketTypeRepository ticketTypeRepo
    ) {
        this.eventRepo = eventRepo;
        this.venueRepo = venueRepo;
        this.ticketTypeRepo = ticketTypeRepo;
    }

    // CREATE
    public EventDetailsResponse create(CreateEventRequest request) {
        Venue venue = venueRepo.findById(request.getVenueId())
                .orElseThrow(() -> notFound("Venue", request.getVenueId()));

        Event e = Event.builder()
                .name(request.getName())
                .startsAt(request.getStartsAt())
                .venue(venue)
                .description(request.getDescription())
                .build();

        Event saved = eventRepo.save(e);
        return buildDetails(saved.getId());
    }

    // READ (details)
    public EventResponse getEventById(Long id) {
        Event e = eventRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + id));
        return mapToSummary(e);
    }

    private EventResponse mapToSummary(Event e) {
        return EventResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .startsAt(e.getStartsAt())
                .venue(org.example.eventpal.dto.venue.VenueSummary.builder()
                        .id(e.getVenue().getId())
                        .venueName(e.getVenue().getName())
                        .build())
                .description(e.getDescription())
                .build();
    }


    // SEARCH (summary list)
    public Page<EventResponse> search(SearchEventRequest req, Pageable pageable) {
        return eventRepo.search(
                emptyToNull(req.getQ()),
                req.getVenueId(),
                req.getStartsAtFrom(),
                req.getStartsAtTo(),
                pageable
        ).map(this::toSummary);
    }

    // UPDATE (partial)
    public EventDetailsResponse update(Long id, UpdateEventRequest request) {
        Event e = eventRepo.findById(id).orElseThrow(() -> notFound("Event", id));

        if (request.getName() != null) e.setName(request.getName());
        if (request.getStartsAt() != null) e.setStartsAt(request.getStartsAt());
        if (request.getDescription() != null) e.setDescription(request.getDescription());

        if (request.getVenueId() != null && !Objects.equals(e.getVenue().getId(), request.getVenueId())) {
            Venue v = venueRepo.findById(request.getVenueId())
                    .orElseThrow(() -> notFound("Venue", request.getVenueId()));
            e.setVenue(v);
        }

        // JPA dirty checking persists
        return buildDetails(e.getId());
    }

    // DELETE
    public void delete(Long id) {
        if (!eventRepo.existsById(id)) throw notFound("Event", id);
        eventRepo.deleteById(id);
    }

    // ------------------ private helpers (kept inside service) ------------------


    private EventDetailsResponse buildDetails(Long id) {
        Event e = eventRepo.findById(id)
                .orElseThrow(() -> notFound("Event", id));

        var types = ticketTypeRepo.findByEvent_Id(id);

        List<TicketTypeResponse> ticketTypes = types.stream()
                .map(tt -> TicketTypeResponse.builder()
                        .id(tt.getId())
                        .event(IdNameDTO.builder().id(e.getId()).name(e.getName()).build())
                        .name(tt.getName())
                        .capacity(tt.getCapacity())
                        .price(toMoneyDTO(tt.getCurrency(), tt.getPrice()))
                        .salesStart(tt.getSalesStart())
                        .salesEnd(tt.getSalesEnd())
                        .active(tt.isActive())
                        .version(tt.getVersion())
                        .build())
                .toList();

        EventInventoryStats stats = computeStats(e.getId(), ticketTypes);

        return EventDetailsResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .startsAt(e.getStartsAt())
                .venue(VenueResponse.builder()
                        .id(e.getVenue().getId())
                        .name(e.getVenue().getName())
                        .address(e.getVenue().getAddress())
                        .seatCount(e.getVenue().getSeatCount())
                        .build())
                .description(e.getDescription())
                .ticketTypes(ticketTypes)
                .stats(stats)
                .build();
    }



    private MoneyDTO toMoneyDTO(String currency, java.math.BigDecimal amount) {
        if (currency == null && amount == null) return null;
        // Normalize currency and scale amount safely for JSON/UI
        String cur = (currency == null) ? null : currency.trim().toUpperCase();
        java.math.BigDecimal price = (amount == null) ? java.math.BigDecimal.ZERO
                : amount.setScale(2, java.math.RoundingMode.HALF_UP);

        return MoneyDTO.builder()
                .currency(cur)
                .price(price)
                .build();
    }

    private EventInventoryStats computeStats(Long eventId, List<TicketTypeResponse> types) {
        int totalCapacity = types.stream().mapToInt(TicketTypeResponse::getCapacity).sum();
        // If you don't yet track sold tickets, set 0 for now:
        int ticketsSold = 0;
        return EventInventoryStats.builder()
                .totalCapacity(totalCapacity)
                .ticketsSold(ticketsSold)
                .ticketsAvailable(Math.max(0, totalCapacity - ticketsSold))
                .build();
    }

    private EventResponse toSummary(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .startsAt(event.getStartsAt())
                .venue(org.example.eventpal.dto.venue.VenueSummary.builder()
                        .id(event.getVenue().getId())
                        .venueName(event.getVenue().getName())
                        .build())
                .description(event.getDescription())
                .build();
    }

    private static ResponseStatusException notFound(String type, Object id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " not found: " + id);
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}