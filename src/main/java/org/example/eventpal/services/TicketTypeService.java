package org.example.eventpal.services;

import jakarta.validation.constraints.DecimalMin;
import org.example.eventpal.dto.ticketType.CreateTicketTypeRequest;
import org.example.eventpal.dto.ticketType.TicketTypeResponse;
import org.example.eventpal.dto.ticketType.UpdateTicketTypeRequest;
import org.example.eventpal.entities.Event;
import org.example.eventpal.entities.TicketType;
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
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    private final TicketRepository  ticketRepository;

    private final EventRepository  eventRepository;

    public TicketTypeService(TicketTypeRepository ticketTypeRepository, TicketRepository ticketRepository, EventRepository eventRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
    }
    // CREATE
    public TicketTypeResponse create(CreateTicketTypeRequest req) {
        Event event = eventRepository.findById(req.getEventId())
                .orElseThrow(() -> notFound("Event", req.getEventId()));

        // unique name per event
        if (ticketTypeRepository.existsByEvent_IdAndNameIgnoreCase(event.getId(), req.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ticket type name already exists for this event");
        }

        TicketType tt = TicketType.builder()
                .event(event)
                .name(req.getName().trim())
                .capacity(req.getCapacity())
                .price(scale(req.getPrice().getPrice()))
                .currency(req.getPrice().getCurrency().trim().toUpperCase())
                .salesStart(req.getSalesStart())
                .salesEnd(req.getSalesEnd())
                .active(req.getActive() == null ? true : req.getActive())
                .build();

        return toResponse(ticketTypeRepository.save(tt));
    }

    // READ
    public TicketTypeResponse get(Long id) {
        TicketType tt = ticketTypeRepository.findById(id).orElseThrow(() -> notFound("TicketType", id));
        return toResponse(tt);
    }

    // LIST by event
    public List<TicketTypeResponse> listByEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) throw notFound("Event", eventId);
        return ticketTypeRepository.findByEvent_Id(eventId).stream().map(this::toResponse).toList();
    }

    // UPDATE (full/partial via same method)
    public TicketTypeResponse update(Long id, UpdateTicketTypeRequest req) {
        TicketType tt = ticketTypeRepository.findById(id).orElseThrow(() -> notFound("TicketType", id));

        if (req.getName() != null) {
            String newName = req.getName().trim();
            if (ticketTypeRepository.existsByEvent_IdAndNameIgnoreCaseAndIdNot(tt.getEvent().getId(), newName, tt.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ticket type name already exists for this event");
            }
            tt.setName(newName);
        }
        if (req.getCapacity() != null) {
            tt.setCapacity(req.getCapacity());
        }
        if (req.getPrice() != null) {
            MoneyDTO m = req.getPrice();
            if (m.getCurrency() != null) tt.setCurrency(m.getCurrency().trim().toUpperCase());
            if (m.getPrice() != null) tt.setPrice(scale(m.getPrice()));
        }
        if (req.getSalesStart() != null) tt.setSalesStart(req.getSalesStart());
        if (req.getSalesEnd() != null)   tt.setSalesEnd(req.getSalesEnd());
        if (req.getActive() != null)     tt.setActive(req.getActive());

        // JPA dirty checking
        return toResponse(tt);
    }

    // DELETE
    public void delete(Long id) {
        TicketType tt = ticketTypeRepository.findById(id)
                .orElseThrow(() -> notFound("TicketType", id));
        tt.setActive(false); // soft-delete
        // JPA dirty check persists
    }

    // --- mapping/local helpers ---

    private TicketTypeResponse toResponse(TicketType t) {
        return TicketTypeResponse.builder()
                .id(t.getId())
                .event(IdNameDTO.builder()
                        .id(t.getEvent().getId())
                        .name(t.getEvent().getName())
                        .build())
                .name(t.getName())
                .capacity(t.getCapacity())
                .price(MoneyDTO.builder()
                        .currency(t.getCurrency())
                        .price(scale(t.getPrice()))
                        .build())
                .salesStart(t.getSalesStart())
                .salesEnd(t.getSalesEnd())
                .active(t.isActive())
                .version(t.getVersion())
                .build();
    }

    private static BigDecimal scale(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }

    private static ResponseStatusException notFound(String type, Object id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " not found: " + id);
    }

}
