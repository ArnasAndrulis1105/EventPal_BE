package org.example.eventpal.dto.ticket.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.helpers.IdNameDTO;
import org.example.eventpal.helpers.MoneyDTO;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketReservationResponse {
    private String reservationId;
    private LocalDateTime expiresAt;
    private IdNameDTO event;
    private List<ReservationLineItem> items;
    private MoneyDTO total;
}
