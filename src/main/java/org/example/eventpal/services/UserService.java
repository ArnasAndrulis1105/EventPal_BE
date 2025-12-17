package org.example.eventpal.services;

import lombok.RequiredArgsConstructor;
import org.example.eventpal.dto.user.UpdateUserProfileRequest;
import org.example.eventpal.dto.user.UserProfileResponse;
import org.example.eventpal.entities.User;
import org.example.eventpal.exceptions.UserNotFoundException;
import org.example.eventpal.repositories.EventRepository;
import org.example.eventpal.repositories.TicketRepository;
import org.example.eventpal.repositories.UserRepository;
import org.example.eventpal.repositories.VenueRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    public UserProfileResponse loadUser(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found!"));
        return UserProfileResponse.builder().
                id(user.getId()).
                name(user.getName()).
                username(user.getUsername()).
                email(user.getEmail()).
                phoneNumber(user.getPhoneNumber()).
                profilePicture(user.getProfilePicture()).
                active(user.getActive()).
                creationDate(user.getCreationDate()).
                lastLoginDate(user.getLastLoginDate()).
                role(user.getRole()).
            build();
    }

    public UserProfileResponse loadUserByUsername(String username){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found!"));
        return UserProfileResponse.builder().
                id(user.getId()).
                name(user.getName()).
                username(user.getUsername()).
                email(user.getEmail()).
                phoneNumber(user.getPhoneNumber()).
                profilePicture(user.getProfilePicture()).
                active(user.getActive()).
                creationDate(user.getCreationDate()).
                lastLoginDate(user.getLastLoginDate()).
                role(user.getRole()).
                build();
    }
    public void delete(Long id){
        if(userRepository.findById(id).isPresent()){
            userRepository.deleteById(id);
        } else if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found!");
        }
    }
    public UserProfileResponse updateUser(Long id, UpdateUserProfileRequest userProfileResponse){
        validateData(id, userProfileResponse);
        User existingUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found!"));

        existingUser.setName(userProfileResponse.getName());
        existingUser.setUsername(userProfileResponse.getUsername());
        existingUser.setEmail(userProfileResponse.getEmail());
        existingUser.setPhoneNumber(userProfileResponse.getPhoneNumber());
        existingUser.setProfilePicture(userProfileResponse.getProfilePicture());

        User updatedUser = userRepository.save(existingUser);

        return UserProfileResponse.builder().
                id(updatedUser.getId()).
                name(updatedUser.getName()).
                username(updatedUser.getUsername()).
                email(updatedUser.getEmail()).
                phoneNumber(updatedUser.getPhoneNumber()).
                profilePicture(updatedUser.getProfilePicture()).
                active(updatedUser.getActive()).
                creationDate(updatedUser.getCreationDate()).
                lastLoginDate(updatedUser.getLastLoginDate()).
                role(updatedUser.getRole()).
                build();
    }

    private void validateData(Long id, UpdateUserProfileRequest req) {
        if (req.getId() != null && !req.getId().equals(id)) {
            throw new IllegalArgumentException("User ID mismatch!");
        }
    }
    public UserProfileResponse loadUserByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));
        return toProfile(user);
    }

    public UserProfileResponse updateUserByEmail(String email, UpdateUserProfileRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setProfilePicture(req.getProfilePicture());
        // ⚠️ I'd strongly avoid changing email here unless you re-issue token and check uniqueness.

        return toProfile(userRepository.save(user));
    }

    private UserProfileResponse toProfile(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePicture(user.getProfilePicture())
                .active(user.getActive())
                .creationDate(user.getCreationDate())
                .lastLoginDate(user.getLastLoginDate())
                .role(user.getRole())
                .build();
    }

}
