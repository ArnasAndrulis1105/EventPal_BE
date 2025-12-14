package org.example.eventpal.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.dto.ticketType.TicketTypeResponse;
import org.example.eventpal.enumerators.TicketStatus;
import org.example.eventpal.helpers.IdNameDTO;
import org.example.eventpal.helpers.MoneyDTO;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private long id;
    private String description;
    private long seat;
    private MoneyDTO pricePaid;
    private LocalDateTime dateTime; // drop if mirroring event.startsAt
    private TicketStatus ticketStatus;
    private IdNameDTO event;
    private TicketTypeResponse ticketType;
}
