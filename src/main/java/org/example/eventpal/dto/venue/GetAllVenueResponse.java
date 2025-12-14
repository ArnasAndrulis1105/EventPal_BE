package org.example.eventpal.dto.venue;

import lombok.Data;
import org.example.eventpal.entities.Venue;

import java.util.List;

@Data
public class GetAllVenueResponse {
    List<VenueSummary> allVenues;
}
