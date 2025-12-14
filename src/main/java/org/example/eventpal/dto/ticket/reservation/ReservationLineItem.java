package org.example.eventpal.dto.ticket.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.helpers.MoneyDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationLineItem {
    private Long ticketTypeId;
    private String ticketTypeName;
    private int quantity;
    private MoneyDTO unitPrice;
    private MoneyDTO lineTotal;
}
