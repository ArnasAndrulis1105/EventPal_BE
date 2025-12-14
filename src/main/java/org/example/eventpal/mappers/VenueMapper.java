package org.example.eventpal.mappers;

import jakarta.persistence.Column;
import org.example.eventpal.dto.venue.VenueSummary;
import org.example.eventpal.entities.Venue;
import org.springframework.stereotype.Component;

@Component
public class VenueMapper {

    public static VenueSummary getVenueSummary(Venue venue){
        return VenueSummary.builder()
                .id(venue.getId())
                .venueAddress(venue.getAddress())
                .venueName(venue.getName())
                .seatCount(venue.getSeatCount())
                .build();
    }
}
