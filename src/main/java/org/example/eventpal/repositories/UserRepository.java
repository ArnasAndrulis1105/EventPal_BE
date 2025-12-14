package org.example.eventpal.repositories;

import org.example.eventpal.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail (String email);

    Optional<User> findByUsername (String username);

    boolean existsByEmail (String email);
}
