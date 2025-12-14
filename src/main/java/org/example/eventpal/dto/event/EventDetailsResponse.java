package org.example.eventpal.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.dto.ticketType.TicketTypeResponse;
import org.example.eventpal.dto.venue.VenueResponse;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailsResponse {
    private Long id;
    private String name;
    private LocalDateTime startsAt;
    private VenueResponse venue;
    private String description;
    private List<TicketTypeResponse> ticketTypes;
    private EventInventoryStats stats;
}
