package com.event.snappro.SnapPro.service.impl;

import com.event.snappro.SnapPro.dto.*;
import com.event.snappro.SnapPro.entity.*;
import com.event.snappro.SnapPro.event.AssignmentChangedEvent;
import com.event.snappro.SnapPro.repository.*;
import com.event.snappro.SnapPro.service.PhotographerAssignmentService;
import com.event.snappro.SnapPro.PhotographerAssignmentStrategy.ConflictCheckStrategy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotographerAssignmentServiceImpl implements PhotographerAssignmentService {

    private static final String OPS_MANAGER = "OperationsManager";
    private static final String ADMIN = "Admin";

    private final PhotographerAssignmentRepository assignmentRepository;
    private final PhotographerProfileRepository profileRepository;
    private final EventBookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final List<ConflictCheckStrategy> conflictStrategies;
    private final ApplicationEventPublisher eventPublisher;

    // ------------------------------------------------------------------ queries

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO getAssignableEvents(HttpServletRequest request) {
        requireRole(request, OPS_MANAGER, ADMIN);
        List<AssignableEventDTO> list = assignmentRepository.findAssignableBookings(LocalDate.now()).stream()
                .map(b -> {
                    List<String> names = assignmentRepository.findByBooking_IdAndActiveTrue(b.getId()).stream()
                            .map(a -> photographerName(a.getPhotographer()))
                            .collect(Collectors.toList());
                    String customer = b.getCustomer() == null ? "" :
                            b.getCustomer().getFirstName() + " " + b.getCustomer().getLastName();
                    return new AssignableEventDTO(b.getId(), b.getEventTitle(), b.getEventLocation(),
                            b.getEventDate(), b.getStartTime(), b.getEndTime(), customer, names.size(), names);
                })
                .collect(Collectors.toList());
        return new ResponseDTO(true, "Upcoming confirmed events loaded.", list);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO getPhotographerAvailability(Integer bookingId, HttpServletRequest request) {
        requireRole(request, OPS_MANAGER, ADMIN);
        Optional<EventBookingEntity> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            return new ResponseDTO(false, "Booking not found with ID: " + bookingId, null);
        }
        List<PhotographerAvailabilityDTO> result = profileRepository.findAll().stream()
                .filter(p -> isActiveUser(p.getUser()))
                .map(p -> {
                    List<String> conflicts = findConflicts(p, booking.get());
                    boolean previously = assignmentRepository
                            .existsByBooking_IdAndPhotographer_IdAndActiveFalse(bookingId, p.getId());
                    return new PhotographerAvailabilityDTO(p.getId(), photographerName(p), p.getUser().getEmail(),
                            p.getExperienceYears(), p.getBio(), conflicts.isEmpty(), conflicts, previously);
                })
                .collect(Collectors.toList());
        return new ResponseDTO(true, "Photographer availability checked.", result);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO getAssignmentsForBooking(Integer bookingId, HttpServletRequest request) {
        requireRole(request, OPS_MANAGER, ADMIN);
        List<AssignmentResponseDTO> list = assignmentRepository.findByBooking_IdAndActiveTrue(bookingId).stream()
                .map(this::toDTO).collect(Collectors.toList());
        return new ResponseDTO(true, "Assignments loaded.", list);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO getRoster(HttpServletRequest request) {
        requireRole(request, OPS_MANAGER, ADMIN);
        List<AssignmentResponseDTO> list = assignmentRepository
                .findByActiveTrueOrderByBooking_EventDateAscBooking_StartTimeAsc().stream()
                .map(this::toDTO).collect(Collectors.toList());
        return new ResponseDTO(true, "Shift roster loaded.", list);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO getPhotographerSchedule(Integer photographerId, HttpServletRequest request) {
        requireRole(request, OPS_MANAGER, ADMIN);
        List<AssignmentResponseDTO> list = assignmentRepository
                .findByPhotographer_IdAndActiveTrueOrderByBooking_EventDateAscBooking_StartTimeAsc(photographerId).stream()
                .map(this::toDTO).collect(Collectors.toList());
        return new ResponseDTO(true, "Photographer schedule loaded.", list);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO getAllPhotographers(HttpServletRequest request) {
        requireRole(request, OPS_MANAGER, ADMIN);
        LocalDate today = LocalDate.now();
        List<PhotographerSummaryDTO> list = profileRepository.findAll().stream()
                .map(p -> {
                    UserEntity u = p.getUser();
                    long upcoming = assignmentRepository
                            .findByPhotographer_IdAndActiveTrueOrderByBooking_EventDateAscBooking_StartTimeAsc(p.getId())
                            .stream()
                            .filter(a -> !a.getBooking().getEventDate().isBefore(today))
                            .count();
                    return new PhotographerSummaryDTO(p.getId(), photographerName(p), u.getEmail(), u.getMobile(),
                            p.getExperienceYears(), p.getBio(), isActiveUser(u), upcoming);
                })
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .collect(Collectors.toList());
        return new ResponseDTO(true, "Photographers loaded.", list);
    }

    // ----------------------------------------------------------------- commands

    @Override
    public ResponseDTO assign(AssignmentRequestDTO dto, HttpServletRequest request) {
        UserEntity manager = requireRole(request, OPS_MANAGER);
        if (dto == null || dto.getBookingId() == null || dto.getPhotographerId() == null) {
            return new ResponseDTO(false, "Booking and photographer are required.", null);
        }
        EventBookingEntity booking = bookingRepository.findById(dto.getBookingId()).orElse(null);
        if (booking == null) {
            return new ResponseDTO(false, "Booking not found with ID: " + dto.getBookingId(), null);
        }
        ResponseDTO bookingProblem = validateBooking(booking);
        if (bookingProblem != null) return bookingProblem;

        PhotographerProfile photographer = profileRepository.findById(dto.getPhotographerId()).orElse(null);
        if (photographer == null) {
            return new ResponseDTO(false, "Photographer not found with ID: " + dto.getPhotographerId(), null);
        }
        if (!isActiveUser(photographer.getUser())) {
            return new ResponseDTO(false, "Photographer account is not active.", null);
        }
        if (assignmentRepository.existsByBooking_IdAndPhotographer_IdAndActiveTrue(booking.getId(), photographer.getId())) {
            return new ResponseDTO(false, "This photographer is already assigned to this event.", null);
        }
        List<String> conflicts = findConflicts(photographer, booking);
        if (!conflicts.isEmpty()) {
            return new ResponseDTO(false, "Scheduling conflict: " + String.join("; ", conflicts), conflicts);
        }

        PhotographerAssignmentEntity saved = assignmentRepository.save(newAssignment(booking, photographer, manager, null));
        publish(AssignmentChangedEvent.Type.ASSIGNED, saved, null);
        return new ResponseDTO(true, "Photographer assigned successfully.", toDTO(saved));
    }

    @Override
    public ResponseDTO reassign(Integer assignmentId, ReassignRequestDTO dto, HttpServletRequest request) {
        UserEntity manager = requireRole(request, OPS_MANAGER);
        if (dto == null || dto.getNewPhotographerId() == null) {
            return new ResponseDTO(false, "New photographer is required.", null);
        }
        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            return new ResponseDTO(false, "A reassignment reason is required.", null);
        }
        PhotographerAssignmentEntity old = assignmentRepository.findById(assignmentId).orElse(null);
        if (old == null || !Boolean.TRUE.equals(old.getActive())) {
            return new ResponseDTO(false, "Active assignment not found with ID: " + assignmentId, null);
        }
        EventBookingEntity booking = old.getBooking();
        ResponseDTO bookingProblem = validateBooking(booking);
        if (bookingProblem != null) return bookingProblem;

        // Rule: crew is locked on the day of the event
        if (booking.getEventDate().isEqual(LocalDate.now())) {
            return new ResponseDTO(false, "This event is today. Crew can no longer be reassigned.", null);
        }

        PhotographerProfile next = profileRepository.findById(dto.getNewPhotographerId()).orElse(null);
        if (next == null) {
            return new ResponseDTO(false, "Photographer not found with ID: " + dto.getNewPhotographerId(), null);
        }
        if (next.getId().equals(old.getPhotographer().getId())) {
            return new ResponseDTO(false, "Choose a different photographer.", null);
        }
        // Rule: a photographer who was replaced/removed from this event cannot be brought back
        if (assignmentRepository.existsByBooking_IdAndPhotographer_IdAndActiveFalse(booking.getId(), next.getId())) {
            return new ResponseDTO(false, photographerName(next)
                    + " was previously replaced on this event and cannot be reassigned to it again.", null);
        }
        if (!isActiveUser(next.getUser())) {
            return new ResponseDTO(false, "Photographer account is not active.", null);
        }
        if (assignmentRepository.existsByBooking_IdAndPhotographer_IdAndActiveTrue(booking.getId(), next.getId())) {
            return new ResponseDTO(false, "That photographer is already assigned to this event.", null);
        }
        List<String> conflicts = findConflicts(next, booking);
        if (!conflicts.isEmpty()) {
            return new ResponseDTO(false, "Scheduling conflict: " + String.join("; ", conflicts), conflicts);
        }

        String reason = dto.getReason().trim();
        old.setActive(false);
        old.setReassignmentReason(reason);
        assignmentRepository.save(old);
        publish(AssignmentChangedEvent.Type.REMOVED, old, reason);

        PhotographerAssignmentEntity saved = assignmentRepository.save(newAssignment(booking, next, manager, reason));
        publish(AssignmentChangedEvent.Type.REASSIGNED, saved, reason);
        return new ResponseDTO(true, "Photographer reassigned successfully.", toDTO(saved));
    }

    @Override
    public ResponseDTO remove(Integer assignmentId, String reason, HttpServletRequest request) {
        requireRole(request, OPS_MANAGER);
        if (reason == null || reason.trim().isEmpty()) {
            return new ResponseDTO(false, "A reason is required to remove an assignment.", null);
        }
        PhotographerAssignmentEntity a = assignmentRepository.findById(assignmentId).orElse(null);
        if (a == null || !Boolean.TRUE.equals(a.getActive())) {
            return new ResponseDTO(false, "Active assignment not found with ID: " + assignmentId, null);
        }
        // Rule: crew is locked on the day of the event
        if (a.getBooking().getEventDate().isEqual(LocalDate.now())) {
            return new ResponseDTO(false, "This event is today. Crew can no longer be removed.", null);
        }
        a.setActive(false);
        a.setReassignmentReason(reason.trim());
        assignmentRepository.save(a);
        publish(AssignmentChangedEvent.Type.REMOVED, a, reason.trim());
        return new ResponseDTO(true, "Assignment removed.", toDTO(a));
    }

    // ------------------------------------------------------------------ helpers

    private List<String> findConflicts(PhotographerProfile p, EventBookingEntity booking) {
        return conflictStrategies.stream()
                .map(s -> s.check(p, booking))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private ResponseDTO validateBooking(EventBookingEntity booking) {
        String status = booking.getBookingStatus() == null ? "" : booking.getBookingStatus().getBookingStatus();
        if (!"confirmed".equalsIgnoreCase(status)) {
            return new ResponseDTO(false, "Only confirmed bookings can be staffed (current status: " + status + ").", null);
        }
        if (booking.getEventDate().isBefore(LocalDate.now())) {
            return new ResponseDTO(false, "This event is in the past.", null);
        }
        return null;
    }

    private PhotographerAssignmentEntity newAssignment(EventBookingEntity booking, PhotographerProfile p,
                                                       UserEntity manager, String reason) {
        PhotographerAssignmentEntity a = new PhotographerAssignmentEntity();
        a.setBooking(booking);
        a.setPhotographer(p);
        a.setAssignedBy(manager);
        a.setActive(true);
        a.setReassignmentReason(reason);
        return a;
    }

    private void publish(AssignmentChangedEvent.Type type, PhotographerAssignmentEntity a, String reason) {
        EventBookingEntity b = a.getBooking();
        eventPublisher.publishEvent(new AssignmentChangedEvent(type, a.getId(), b.getId(), b.getEventTitle(),
                b.getEventDate(), b.getStartTime(), b.getEndTime(), photographerName(a.getPhotographer()),
                a.getPhotographer().getUser().getEmail(), reason));
    }

    private AssignmentResponseDTO toDTO(PhotographerAssignmentEntity a) {
        EventBookingEntity b = a.getBooking();
        String by = a.getAssignedBy() == null ? "" :
                a.getAssignedBy().getFirstName() + " " + a.getAssignedBy().getLastName();
        return new AssignmentResponseDTO(a.getId(), b.getId(), b.getEventTitle(), b.getEventLocation(),
                b.getEventDate(), b.getStartTime(), b.getEndTime(), a.getPhotographer().getId(),
                photographerName(a.getPhotographer()), by, a.getAssignedAt(),
                Boolean.TRUE.equals(a.getActive()), a.getReassignmentReason());
    }

    private String photographerName(PhotographerProfile p) {
        return p.getUser().getFirstName() + " " + p.getUser().getLastName();
    }

    private boolean isActiveUser(UserEntity u) {
        return u.getUserStatus() == null || "ACTIVE".equalsIgnoreCase(u.getUserStatus().getUserStatus());
    }

    private UserEntity requireRole(HttpServletRequest request, String... allowedRoles) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in.");
        }
        String role = (String) session.getAttribute("userRole");
        boolean allowed = false;
        for (String r : allowedRoles) {
            if (r.equalsIgnoreCase(role)) { allowed = true; break; }
        }
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission for this action.");
        }
        return userRepository.findById((Integer) session.getAttribute("userId"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found."));
    }
}