package org.example.eventpal.dto.ticket.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.enumerators.TicketStatus;
import org.example.eventpal.helpers.MoneyDTO;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTicketRequest {
    private Long ticketTypeId;
    private Long seat;
    private String description;
    private MoneyDTO pricePaid;
    private TicketStatus status;
}
