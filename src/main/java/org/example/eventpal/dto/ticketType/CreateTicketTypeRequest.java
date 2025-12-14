package org.example.eventpal.dto.ticketType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.example.eventpal.helpers.MoneyDTO;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketTypeRequest {
    @NotNull
    private Long eventId;
    @NotBlank
    private String name;      // "General", "VIP"
    @Positive
    private int capacity;
    @NotNull
    private MoneyDTO price;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private Boolean active;
}
