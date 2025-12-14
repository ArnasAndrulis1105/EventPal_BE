package org.example.eventpal.dto.ticket.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.enumerators.TicketStatus;
import org.example.eventpal.helpers.MoneyDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {
    @NotNull
    @JsonProperty("event_id")
    private Long eventId;

    @NotNull
    @JsonProperty("ticket_type_id")
    private Long ticketTypeId;

    @Positive
    @JsonProperty("seat")
    private long seat;

    @NotBlank
    @JsonProperty("description")
    private String description;

    @NotNull
    @JsonProperty("price_paid")
    private MoneyDTO pricePaid;

    @NotNull
    @JsonProperty("status")
    private TicketStatus status;
}
