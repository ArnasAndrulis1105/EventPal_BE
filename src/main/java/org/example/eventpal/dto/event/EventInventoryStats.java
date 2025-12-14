package org.example.eventpal.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventInventoryStats {
    private int totalCapacity;
    private int ticketsSold;
    private int ticketsAvailable;
}
