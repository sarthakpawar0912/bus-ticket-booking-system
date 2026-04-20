package com.busticketbookingsystem.scheduling;

import com.busticketbookingsystem.booking.repository.BookingRepository;
import com.busticketbookingsystem.payment.repository.PaymentRepository;
import com.busticketbookingsystem.trip.entity.Trip;
import com.busticketbookingsystem.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingMonitoringJob {

    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    // Every hour — operational snapshot for observability.
    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    public void logHourlySnapshot() {
        long bookings = bookingRepository.count();
        long payments = paymentRepository.count();
        long trips = tripRepository.count();
        log.info("[monitor] snapshot — trips={}, bookings={}, payments={}", trips, bookings, payments);
    }

    // Every day at 01:00 — list trips departing in the next 24 hours.
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional(readOnly = true)
    public void logUpcomingTrips() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        List<Trip> all = tripRepository.findAllWithDetails();
        long upcoming = all.stream()
                .filter(t -> t.getTripDate() != null
                        && !t.getTripDate().isBefore(now)
                        && t.getTripDate().isBefore(tomorrow))
                .count();
        log.info("[monitor] {} trip(s) departing in next 24h", upcoming);
    }
}
