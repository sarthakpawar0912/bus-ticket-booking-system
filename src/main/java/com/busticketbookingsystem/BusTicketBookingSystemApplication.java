package com.busticketbookingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // <-- Import this

@SpringBootApplication
@EnableScheduling
public class BusTicketBookingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusTicketBookingSystemApplication.class, args);
	}
}