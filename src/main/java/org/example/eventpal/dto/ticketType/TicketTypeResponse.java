package org.example.eventpal.dto.ticketType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.helpers.IdNameDTO;
import org.example.eventpal.helpers.MoneyDTO;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTypeResponse {
    private Long id;
    private IdNameDTO event;
    private String name;
    private int capacity;
    private MoneyDTO price;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private boolean active;
    private long version;
}
