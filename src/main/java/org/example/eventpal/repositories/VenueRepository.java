package org.example.eventpal.repositories;

import org.example.eventpal.entities.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    boolean existsByName(String name);
}
