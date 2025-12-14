package org.example.eventpal.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchEventRequest {
    private String q;
    private Long venueId;
    private LocalDateTime startsAtFrom;
    private LocalDateTime startsAtTo;
}
