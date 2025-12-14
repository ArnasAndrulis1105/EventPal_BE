package org.example.eventpal.services;

import jakarta.transaction.Transactional;
import org.example.eventpal.dto.ticket.TicketResponse;
import org.example.eventpal.dto.ticket.create.CreateTicketRequest;
import org.example.eventpal.dto.ticketType.TicketTypeResponse;
import org.example.eventpal.entities.Event;
import org.example.eventpal.entities.Ticket;
import org.example.eventpal.entities.TicketType;
import org.example.eventpal.enumerators.TicketStatus;
import org.example.eventpal.helpers.IdNameDTO;
import org.example.eventpal.helpers.MoneyDTO;
import org.example.eventpal.repositories.EventRepository;
import org.example.eventpal.repositories.TicketRepository;
import org.example.eventpal.repositories.TicketTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepo;
    private final EventRepository eventRepo;
    private final TicketTypeRepository ticketTypeRepo;

    public TicketService(TicketRepository ticketRepo,
                         EventRepository eventRepo,
                         TicketTypeRepository ticketTypeRepo) {
        this.ticketRepo = ticketRepo;
        this.eventRepo = eventRepo;
        this.ticketTypeRepo = ticketTypeRepo;
    }

    // CREATE a single ticket (manual issue)
    public TicketResponse create(CreateTicketRequest req) {
        Event event = eventRepo.findById(req.getEventId())
                .orElseThrow(() -> notFound("Event", req.getEventId()));
        TicketType type = ticketTypeRepo.findById(req.getTicketTypeId())
                .orElseThrow(() -> notFound("TicketType", req.getTicketTypeId()));

        // seat uniqueness per event (DB constraint also enforces it)
        if (ticketRepo.existsByEvent_IdAndSeat(event.getId(), req.getSeat())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seat " + req.getSeat() + " already taken for event " + event.getId());
        }

        // Basic capacity guard (best-effort)
        long issuedForType = ticketRepo.countByTicketType_Id(type.getId());
        long remaining = Math.max(0, (long) type.getCapacity() - issuedForType);
        if (remaining <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Capacity reached for ticket type");
        }

        // Price snapshot (entity uses float)
        float priceSnapshot = scale(req.getPricePaid().getPrice()).floatValue();

        Ticket t = Ticket.builder()
                .description(req.getDescription())
                .seat(req.getSeat())
                .price(priceSnapshot)
                .dateTime(event.getStartsAt()) // or LocalDateTime.now()
                .ticketStatus(req.getStatus())
                .event(event)
                .ticketType(type)
                .build();

        return toTicketResponse(ticketRepo.save(t));
    }

    // READ by id
    public TicketResponse get(Long id) {
        Ticket t = ticketRepo.findById(id).orElseThrow(() -> notFound("Ticket", id));
        return toTicketResponse(t);
    }

    // LIST by event
    public List<TicketResponse> listByEvent(Long eventId) {
        // 404 if event doesn’t exist
        if (!eventRepo.existsById(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + eventId);
        }
        return ticketRepo.findByEvent_Id(eventId).stream()
                .map(this::toTicketResponse)
                .toList();
    }

    // PATCH status (lightweight admin op)
    public TicketResponse updateStatus(Long ticketId, TicketStatus newStatus) {
        Ticket t = ticketRepo.findById(ticketId).orElseThrow(() -> notFound("Ticket", ticketId));
        t.setTicketStatus(newStatus);
        return toTicketResponse(t);
    }

    // DELETE (admin)
    public void delete(Long id) {
        if (!ticketRepo.existsById(id)) throw notFound("Ticket", id);
        ticketRepo.deleteById(id);
    }

    // ----------------- mapping helpers (kept inside service) -----------------

    private TicketResponse toTicketResponse(Ticket t) {
        TicketType type = t.getTicketType();

        TicketTypeResponse typeDto = TicketTypeResponse.builder()
                .id(type.getId())
                .event(IdNameDTO.builder().id(t.getEvent().getId()).name(t.getEvent().getName()).build())
                .name(type.getName())
                .capacity(type.getCapacity())
                .price(MoneyDTO.builder()
                        .currency(type.getCurrency())
                        .price(scale(type.getPrice()))
                        .build())
                .salesStart(type.getSalesStart())
                .salesEnd(type.getSalesEnd())
                .active(type.isActive())
                .version(type.getVersion())
                .build();

        return TicketResponse.builder()
                .id(t.getId())
                .description(t.getDescription())
                .seat(t.getSeat())
                .pricePaid(MoneyDTO.builder()
                        .currency(type.getCurrency()) // or store currency on Ticket if needed
                        .price(scale(new BigDecimal(Float.toString(t.getPrice()))))
                        .build())
                .dateTime(t.getDateTime())
                .ticketStatus(t.getTicketStatus())
                .event(IdNameDTO.builder().id(t.getEvent().getId()).name(t.getEvent().getName()).build())
                .ticketType(typeDto)
                .build();
    }

    private static BigDecimal scale(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }

    private static ResponseStatusException notFound(String type, Object id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " not found: " + id);
    }
}
