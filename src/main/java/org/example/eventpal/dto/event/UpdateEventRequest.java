package org.example.eventpal.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventRequest {
    private String name;
    private LocalDateTime startsAt;
    private Long venueId;
    private String description;
}
