package com.event.snappro.SnapPro.service;

import com.event.snappro.SnapPro.dto.BookingRequestDTO;
import com.event.snappro.SnapPro.dto.BookingResponseDTO;
import com.event.snappro.SnapPro.dto.ResponseDTO;
import com.event.snappro.SnapPro.entity.BookingStatusEntity;
import com.event.snappro.SnapPro.entity.EventBookingEntity;
import com.event.snappro.SnapPro.entity.PackageEntity;
import com.event.snappro.SnapPro.entity.PaymentMethodEntity;
import com.event.snappro.SnapPro.entity.UserEntity;
import com.event.snappro.SnapPro.repository.BookingStatusRepository;
import com.event.snappro.SnapPro.repository.EventBookingRepository;
import com.event.snappro.SnapPro.repository.PackageRepository;
import com.event.snappro.SnapPro.repository.PaymentMethodRepository;
import com.event.snappro.SnapPro.repository.UserRepository;
import com.event.snappro.SnapPro.service.impl.BookingServiceImpl;
import com.event.snappro.SnapPro.strategy.payment.PaymentContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private EventBookingRepository eventBookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private BookingStatusRepository bookingStatusRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PaymentContext paymentContext;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private UserEntity testCustomer;
    private PackageEntity testPackage;
    private BookingStatusEntity pendingStatus;
    private PaymentMethodEntity cashPaymentMethod;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        futureDate = LocalDate.now().plusDays(5);

        testCustomer = new UserEntity();
        testCustomer.setId(1);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@example.com");
        testCustomer.setMobile("0771234567");

        testPackage = new PackageEntity();
        testPackage.setId(10);
        testPackage.setTitle("Luxury Wedding Package");
        testPackage.setPrice(75000.0);

        pendingStatus = new BookingStatusEntity();
        pendingStatus.setId(1);
        pendingStatus.setBookingStatus("Pending");

        cashPaymentMethod = new PaymentMethodEntity();
        cashPaymentMethod.setId(1);
        cashPaymentMethod.setPaymentMethod("Cash on Event Date");
    }

    /**
     * TEST 1: Successful Booking Creation (Standard Photography Package)
     * Verifies that when a valid logged-in customer books an available time slot:
     * - The booking is saved with status 'Pending'
     * - Payment strategy is executed
     * - Confirmation email is dispatched
     * - A successful ResponseDTO is returned with mapped details
     */
    @Test
    @DisplayName("Unit Test 1: Successfully create booking for an available time slot")
    void testCreateBooking_Success() {
        // Arrange
        BookingRequestDTO requestDTO = new BookingRequestDTO();
        requestDTO.setPackageId(10);
        requestDTO.setEventTitle("Grand Wedding Shoot");
        requestDTO.setEventLocation("Galle Face Hotel, Colombo");
        requestDTO.setEventDate(futureDate);
        requestDTO.setStartTime(LocalTime.of(10, 0));
        requestDTO.setEndTime(LocalTime.of(14, 0));
        requestDTO.setPaymentMethodId(1);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testCustomer));

        // No conflicting bookings on the selected date
        when(eventBookingRepository.findActiveBookingsByDate(futureDate)).thenReturn(Collections.emptyList());

        when(packageRepository.findById(10)).thenReturn(Optional.of(testPackage));
        when(bookingStatusRepository.findByBookingStatusIgnoreCase("Pending")).thenReturn(Optional.of(pendingStatus));
        when(paymentMethodRepository.findById(1)).thenReturn(Optional.of(cashPaymentMethod));

        EventBookingEntity savedBooking = new EventBookingEntity();
        savedBooking.setId(101);
        savedBooking.setCustomer(testCustomer);
        savedBooking.setPackageEntity(testPackage);
        savedBooking.setEventTitle(requestDTO.getEventTitle());
        savedBooking.setEventDate(futureDate);
        savedBooking.setStartTime(requestDTO.getStartTime());
        savedBooking.setEndTime(requestDTO.getEndTime());
        savedBooking.setTotalAmount(75000.0);
        savedBooking.setPaymentMethod(cashPaymentMethod);
        savedBooking.setBookingStatus(pendingStatus);

        when(eventBookingRepository.save(any(EventBookingEntity.class))).thenReturn(savedBooking);

        BookingResponseDTO mockResponseDTO = new BookingResponseDTO();
        mockResponseDTO.setId(101);
        mockResponseDTO.setEventTitle("Grand Wedding Shoot");
        when(modelMapper.map(any(EventBookingEntity.class), eq(BookingResponseDTO.class))).thenReturn(mockResponseDTO);

        // Act
        ResponseDTO response = bookingService.createBooking(requestDTO, request);

        // Assert
        assertNotNull(response, "ResponseDTO should not be null");
        assertTrue(response.isStatus(), "Response status must be true for valid booking");
        assertEquals("Booking request submitted successfully with status 'Pending'!", response.getMessage());
        assertNotNull(response.getData(), "Response data must contain booking details");

        // Verify key business operations were called
        verify(eventBookingRepository, times(1)).save(any(EventBookingEntity.class));
        verify(paymentContext, times(1)).setStrategyByMethodName("Cash on Event Date");
        verify(paymentContext, times(1)).executePayment(eq(savedBooking), eq(75000.0));
        verify(emailService, times(1)).sendBookingConfirmationEmail(eq("john.doe@example.com"), eq("John Doe"), eq(savedBooking));
    }

    /**
     * TEST 2: Time Slot Overlap Conflict Validation
     * Verifies that when a customer tries to book a slot that overlaps with an already booked active slot
     * on the same date:
     * - The request is blocked with a descriptive conflict message
     * - The booking is NOT saved to the database
     * - No payment or email dispatch takes place
     */
    @Test
    @DisplayName("Unit Test 2: Reject booking request when timeslot overlaps with existing active booking")
    void testCreateBooking_Fails_WhenTimeSlotConflict() {
        // Arrange
        BookingRequestDTO requestDTO = new BookingRequestDTO();
        requestDTO.setPackageId(10);
        requestDTO.setEventTitle("Corporate Gala Dinner");
        requestDTO.setEventLocation("Hilton Colombo");
        requestDTO.setEventDate(futureDate);
        requestDTO.setStartTime(LocalTime.of(13, 0));
        requestDTO.setEndTime(LocalTime.of(16, 0));

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testCustomer));

        // Existing booking on the same date from 14:00 to 18:00 (Overlaps with 13:00 - 16:00)
        EventBookingEntity existingBooking = new EventBookingEntity();
        existingBooking.setId(50);
        existingBooking.setEventDate(futureDate);
        existingBooking.setStartTime(LocalTime.of(14, 0));
        existingBooking.setEndTime(LocalTime.of(18, 0));

        when(eventBookingRepository.findActiveBookingsByDate(futureDate)).thenReturn(List.of(existingBooking));

        // Act
        ResponseDTO response = bookingService.createBooking(requestDTO, request);

        // Assert
        assertNotNull(response);
        assertFalse(response.isStatus(), "Response status must be false when time slot conflict occurs");
        assertTrue(response.getMessage().contains("Time Slot Conflict"), "Message should indicate time slot conflict");
        assertNull(response.getData(), "Data should be null on failure");

        // Verify database and side-effects are NEVER executed
        verify(eventBookingRepository, never()).save(any(EventBookingEntity.class));
        verify(paymentContext, never()).executePayment(any(), anyDouble());
        verify(emailService, never()).sendBookingConfirmationEmail(any(), any(), any());
    }

    /**
     * TEST 3: Unauthorized Customer Access (No Active Session)
     * Verifies security validation: when an unauthenticated customer attempts to create a booking:
     * - Request is immediately rejected with an appropriate sign-in message
     * - No database searches or saves are performed
     */
    @Test
    @DisplayName("Unit Test 3: Reject booking request when user is not signed in")
    void testCreateBooking_Fails_WhenUserNotLoggedIn() {
        // Arrange: No active HTTP session
        BookingRequestDTO requestDTO = new BookingRequestDTO();
        requestDTO.setEventTitle("Private Shoot");
        requestDTO.setEventDate(futureDate);

        when(request.getSession(false)).thenReturn(null);

        // Act
        ResponseDTO response = bookingService.createBooking(requestDTO, request);

        // Assert
        assertNotNull(response);
        assertFalse(response.isStatus(), "Response status must be false for unauthenticated requests");
        assertEquals("Please sign in to submit a booking request.", response.getMessage());
        assertNull(response.getData());

        // Verify no database interaction happened
        verifyNoInteractions(eventBookingRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(paymentContext);
        verifyNoInteractions(emailService);
    }
}
