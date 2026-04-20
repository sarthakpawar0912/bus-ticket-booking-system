package com.busticketbookingsystem.scheduling;

import com.busticketbookingsystem.booking.repository.BookingRepository;
import com.busticketbookingsystem.payment.repository.PaymentRepository;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.repository.TripRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingMonitoringJobTest {

    @Mock private TripRepository tripRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private PaymentRepository paymentRepository;

    @InjectMocks private BookingMonitoringJob job;

    @Test
    @DisplayName("POSITIVE: logHourlySnapshot queries counts on all three repositories")
    void hourlySnapshot() {
        when(tripRepository.count()).thenReturn(50L);
        when(bookingRepository.count()).thenReturn(120L);
        when(paymentRepository.count()).thenReturn(110L);

        job.logHourlySnapshot();

        verify(tripRepository).count();
        verify(bookingRepository).count();
        verify(paymentRepository).count();
    }

    @Test
    @DisplayName("POSITIVE: logUpcomingTrips counts trips departing within 24h")
    void upcomingTrips() {
        LocalDateTime soon = LocalDateTime.now().plusHours(5);
        LocalDateTime later = LocalDateTime.now().plusDays(3);
        LocalDateTime past = LocalDateTime.now().minusDays(1);

        Trip in24h = Trip.builder().tripId(1).tripDate(soon).build();
        Trip farFuture = Trip.builder().tripId(2).tripDate(later).build();
        Trip gone = Trip.builder().tripId(3).tripDate(past).build();

        when(tripRepository.findAllWithDetails()).thenReturn(List.of(in24h, farFuture, gone));

        job.logUpcomingTrips();

        verify(tripRepository).findAllWithDetails();
        // No assertion on log output; this test verifies the method runs
        // without exception and pulls the correct repository data.
    }

    @Test
    @DisplayName("EDGE: logUpcomingTrips handles empty trip list")
    void upcomingTripsEmpty() {
        when(tripRepository.findAllWithDetails()).thenReturn(List.of());

        job.logUpcomingTrips();

        verify(tripRepository).findAllWithDetails();
    }

    @Test
    @DisplayName("EDGE: logUpcomingTrips skips trips with null tripDate")
    void upcomingTripsNullDate() {
        Trip noDate = Trip.builder().tripId(1).tripDate(null).build();
        when(tripRepository.findAllWithDetails()).thenReturn(List.of(noDate));

        job.logUpcomingTrips();

        verify(tripRepository).findAllWithDetails();
    }
}
