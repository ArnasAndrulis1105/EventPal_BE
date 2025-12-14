package org.example.eventpal.dto.ticketType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.helpers.MoneyDTO;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTicketTypeRequest {
    private String name;
    private Integer capacity;
    private MoneyDTO price;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private Boolean active;
}
