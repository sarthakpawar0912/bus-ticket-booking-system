package com.busticketbookingsystem.trip.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Integer routeId;

    @Column(name = "from_city")
    private String fromCity;

    @Column(name = "to_city")
    private String toCity;

    @Column(name = "break_points")
    private Integer breakPoints;

    @Column(name = "duration")
    private Integer duration;
}
