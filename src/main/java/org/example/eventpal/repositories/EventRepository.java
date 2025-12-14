package org.example.eventpal.repositories;

import org.example.eventpal.entities.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event,Long> {
    @Query("""
  select e from Event e
  where lower(e.name) like concat('%', lower(coalesce(:q, '')), '%')
    and (:venueId is null or e.venue.id = :venueId)
    and e.startsAt >= coalesce(:startsAtFrom, e.startsAt)
    and e.startsAt <= coalesce(:startsAtTo,   e.startsAt)
""")
    Page<Event> search(
            @Param("q") String q,
            @Param("venueId") Long venueId,
            @Param("startsAtFrom") java.time.LocalDateTime startsAtFrom,
            @Param("startsAtTo") java.time.LocalDateTime startsAtTo,
            Pageable pageable
    );

    // If you want a non-paged variant:
    List<Event> findAllByVenue_Id(Long venueId);

    // Paged (recommended):
    Page<Event> findByVenue_Id(Long venueId, Pageable pageable);

    // For “join fetch” to avoid LAZY N+1 on venue (optional)
    @Query("""
       select e from Event e
       join fetch e.venue v
       where v.id = :venueId
    """)
    List<Event> findAllWithVenueByVenueId(@Param("venueId") Long venueId);
    long countByVenue_Id(Long venueId);
}
