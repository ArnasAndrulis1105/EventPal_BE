package org.example.eventpal.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eventpal.dto.venue.VenueSummary;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventResponse {

    private Long id;
    private String name;
    private LocalDateTime startsAt;
    private VenueSummary venue;
    private String description;
}
