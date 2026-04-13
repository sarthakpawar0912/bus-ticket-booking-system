package com.busticketbookingsystem.review.entity;

import com.busticketbookingsystem.customer.entity.Customer;
import com.busticketbookingsystem.trip.entity.Trip;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {
    @Id
    @Column(name = "review_id")
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;
}
