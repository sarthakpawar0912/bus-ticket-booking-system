package com.busticketbookingsystem.web.team;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * In-memory registry of the 5 team members. Each member's endpoints map
 * to EXISTING Thymeleaf UI pages - never raw JSON - per the spec.
 */
@Component
public class TeamRegistry {

    private final List<TeamMember> members = buildMembers();

    public List<TeamMember> getAll() { return members; }

    public Optional<TeamMember> findBySlug(String slug) {
        return members.stream().filter(m -> m.getSlug().equalsIgnoreCase(slug)).findFirst();
    }

    private static EndpointEntry ep(String method, String apiPath, String description,
                                    String uiUrl, String actionLabel) {
        return EndpointEntry.builder().method(method).apiPath(apiPath)
                .description(description).uiUrl(uiUrl).actionLabel(actionLabel).build();
    }

    private static ScreenEntry sc(String name, String url, String icon) {
        return ScreenEntry.builder().name(name).url(url).icon(icon).build();
    }

    // --------------------------------------------------------------
    // Member 1: Sarthak Pawar - Customer Module
    // --------------------------------------------------------------
    private TeamMember sarthakPawar() {
        return TeamMember.builder()
                .slug("sarthak-pawar").name("Sarthak Pawar")
                .role("Customer Module Developer")
                .photo("https://api.dicebear.com/7.x/initials/svg?seed=Sarthak+Pawar&backgroundColor=0d6efd")
                .initials("SP").color("#0d6efd")
                .modules(List.of("Customers", "Addresses"))
                .responsibilities(List.of(
                        "Design and maintain the Customer and Address modules",
                        "Implement DTO-based validation for customer input",
                        "Expose REST APIs and Thymeleaf UI screens for customer lifecycle",
                        "Ensure addresses can be safely linked to customers, drivers and offices"))
                .endpoints(List.of(
                        ep("GET ALL", "/api/customers", "List every registered customer",
                                "/view/customers", "Open List"),
                        ep("POST", "/api/customers", "Register a new customer",
                                "/view/customers/add", "Open Add Form"),
                        ep("PUT", "/api/customers/{id}", "Edit an existing customer",
                                "/view/customers", "Open List to Edit"),
                        ep("GET ALL", "/api/addresses", "List every saved address",
                                "/view/addresses", "Open List"),
                        ep("POST", "/api/addresses", "Register a new address",
                                "/view/addresses/add", "Open Add Form"),
                        ep("PUT", "/api/addresses/{id}", "Edit an existing address",
                                "/view/addresses", "Open List to Edit")))
                .screens(List.of(
                        sc("Customers List", "/view/customers", "bi-people-fill"),
                        sc("Add Customer", "/view/customers/add", "bi-person-plus-fill"),
                        sc("Addresses List", "/view/addresses", "bi-geo-alt-fill"),
                        sc("Add Address", "/view/addresses/add", "bi-pin-map-fill")))
                .build();
    }

    // --------------------------------------------------------------
    // Member 2: Atharv Kadam - Agency Module
    // --------------------------------------------------------------
    private TeamMember atharvKadam() {
        return TeamMember.builder()
                .slug("atharv-kadam").name("Atharv Kadam")
                .role("Agency Module Developer")
                .photo("https://api.dicebear.com/7.x/initials/svg?seed=Atharv+Kadam&backgroundColor=198754")
                .initials("AK").color("#198754")
                .modules(List.of("Agencies", "Offices", "Buses", "Drivers"))
                .responsibilities(List.of(
                        "Manage the Agency + Office hierarchy and related entities",
                        "Implement uniqueness validation for emails, phones, registration & license numbers",
                        "Expose REST APIs and Thymeleaf UIs for bus and driver lifecycle",
                        "Coordinate the safe-delete safety checks across agencies, offices, buses and drivers"))
                .endpoints(List.of(
                        ep("GET ALL", "/api/agencies", "List every registered agency",
                                "/view/agencies", "Open List"),
                        ep("POST", "/api/agencies", "Register a new agency",
                                "/view/agencies/add", "Open Add Form"),
                        ep("PUT", "/api/agencies/{id}", "Edit an existing agency",
                                "/view/agencies", "Open List to Edit"),
                        ep("GET ALL", "/api/offices", "List every office",
                                "/view/offices", "Open List"),
                        ep("POST", "/api/offices", "Register a new office",
                                "/view/offices/add", "Open Add Form"),
                        ep("PUT", "/api/offices/{id}", "Edit an existing office",
                                "/view/offices", "Open List to Edit"),
                        ep("GET ALL", "/api/buses", "List every registered bus",
                                "/view/buses", "Open List"),
                        ep("POST", "/api/buses", "Register a new bus",
                                "/view/buses/add", "Open Add Form"),
                        ep("PUT", "/api/buses/{id}", "Edit an existing bus",
                                "/view/buses", "Open List to Edit"),
                        ep("GET ALL", "/api/drivers", "List every registered driver",
                                "/view/drivers", "Open List"),
                        ep("POST", "/api/drivers", "Register a new driver",
                                "/view/drivers/add", "Open Add Form"),
                        ep("PUT", "/api/drivers/{id}", "Edit an existing driver",
                                "/view/drivers", "Open List to Edit")))
                .screens(List.of(
                        sc("Agencies List", "/view/agencies", "bi-building"),
                        sc("Add Agency", "/view/agencies/add", "bi-building-add"),
                        sc("Offices List", "/view/offices", "bi-shop"),
                        sc("Add Office", "/view/offices/add", "bi-shop-window"),
                        sc("Buses List", "/view/buses", "bi-bus-front-fill"),
                        sc("Add Bus", "/view/buses/add", "bi-bus-front"),
                        sc("Drivers List", "/view/drivers", "bi-person-badge"),
                        sc("Add Driver", "/view/drivers/add", "bi-person-badge-fill")))
                .build();
    }

    // --------------------------------------------------------------
    // Member 3: Atharva Pawar - Trip Module
    // --------------------------------------------------------------
    private TeamMember atharvaPawar() {
        return TeamMember.builder()
                .slug("atharva-pawar").name("Atharva Pawar")
                .role("Trip Module Developer")
                .photo("https://api.dicebear.com/7.x/initials/svg?seed=Atharva+Pawar&backgroundColor=fd7e14")
                .initials("AP").color("#fd7e14")
                .modules(List.of("Routes", "Trips", "Search Routes", "Search Trips"))
                .responsibilities(List.of(
                        "Design and maintain the Route and Trip modules",
                        "Implement case-insensitive city search for routes and trips",
                        "Wire up trip creation with 6 foreign keys (route, bus, 2 drivers, 2 addresses)",
                        "Expose Thymeleaf UIs that let the operator schedule and search journeys"))
                .endpoints(List.of(
                        ep("GET ALL", "/api/routes", "List every route", "/view/routes", "Open List"),
                        ep("POST", "/api/routes", "Add a new route", "/view/routes/add", "Open Add Form"),
                        ep("PUT", "/api/routes/{id}", "Edit a route", "/view/routes", "Open List to Edit"),
                        ep("SEARCH", "/api/routes/search", "Search routes by from/to city",
                                "/view/routes/search", "Open Search"),
                        ep("GET ALL", "/api/trips", "List every scheduled trip", "/view/trips", "Open List"),
                        ep("POST", "/api/trips", "Schedule a new trip", "/view/trips/add", "Open Add Form"),
                        ep("PUT", "/api/trips/{id}", "Edit an existing trip",
                                "/view/trips", "Open List to Edit"),
                        ep("SEARCH", "/api/trips/search", "Search trips by from/to city",
                                "/view/trips/search", "Open Search")))
                .screens(List.of(
                        sc("Routes List", "/view/routes", "bi-signpost-split"),
                        sc("Add Route", "/view/routes/add", "bi-signpost-2"),
                        sc("Search Routes", "/view/routes/search", "bi-search"),
                        sc("Trips List", "/view/trips", "bi-calendar-event"),
                        sc("Add Trip", "/view/trips/add", "bi-calendar-plus"),
                        sc("Search Trips", "/view/trips/search", "bi-search-heart")))
                .build();
    }

    // --------------------------------------------------------------
    // Member 4: Anushka Bankar - Payment Module
    // --------------------------------------------------------------
    private TeamMember anushkaBankar() {
        return TeamMember.builder()
                .slug("anushka-bankar").name("Anushka Bankar")
                .role("Payment Module Developer")
                .photo("https://api.dicebear.com/7.x/initials/svg?seed=Anushka+Bankar&backgroundColor=6f42c1")
                .initials("AB").color("#6f42c1")
                .modules(List.of("Payments", "Refund", "Ticket Download", "Group Ticket Download"))
                .responsibilities(List.of(
                        "Design and maintain the Payment module",
                        "Implement payment processing and refund flows",
                        "Wire up PDF ticket download and group-ticket download UIs",
                        "Build the payment checkout and success-page Thymeleaf views"))
                .endpoints(List.of(
                        ep("GET ALL", "/api/payments", "List every payment", "/view/payments", "Open List"),
                        ep("POST", "/api/payments", "Process a new payment (via checkout)",
                                "/view/payments", "Open Payments"),
                        ep("POST", "/api/payments/{id}/refund", "Refund an existing payment",
                                "/view/payments/refund", "Open Refund"),
                        ep("DOWNLOAD", "/api/payments/{id}/ticket", "Download a payment ticket PDF",
                                "/view/payments/ticket", "Download Ticket"),
                        ep("DOWNLOAD", "/api/payments/group-ticket",
                                "Download group ticket PDF for multiple payments",
                                "/view/payments/group-ticket", "Download Group Ticket")))
                .screens(List.of(
                        sc("Payments List", "/view/payments", "bi-credit-card"),
                        sc("Refund Confirmation", "/view/payments/refund", "bi-arrow-counterclockwise"),
                        sc("Ticket Download", "/view/payments/ticket", "bi-file-earmark-pdf"),
                        sc("Group Ticket Download", "/view/payments/group-ticket", "bi-files")))
                .build();
    }

    // --------------------------------------------------------------
    // Member 5: Kedar Mahadik - Booking + Reviews
    // --------------------------------------------------------------
    private TeamMember kedarMahadik() {
        return TeamMember.builder()
                .slug("kedar-mahadik").name("Kedar Mahadik")
                .role("Booking Developer")
                .photo("https://api.dicebear.com/7.x/initials/svg?seed=Kedar+Mahadik&backgroundColor=dc3545")
                .initials("KM").color("#dc3545")
                .modules(List.of("Bookings", "Cancel Booking", "Ticket Download",
                        "Group Booking Ticket", "Reviews"))
                .responsibilities(List.of(
                        "Design and maintain the Booking + Reviews modules",
                        "Build the full end-user booking flow: search, seat selection, confirmation",
                        "Implement atomic multi-seat booking with safe cancellation",
                        "Wire up the PDF ticket flow and the group-ticket download",
                        "Expose review creation UI and review listing"))
                .endpoints(List.of(
                        ep("GET ALL", "/api/bookings/trip/{tripId}", "List seats/bookings for a trip",
                                "/view/bookings", "Open Booking Flow"),
                        ep("POST", "/api/bookings", "Book one or more seats on a trip",
                                "/view/bookings", "Open Booking Flow"),
                        ep("POST", "/api/bookings/{id}/cancel", "Cancel an existing booking",
                                "/view/bookings/cancel", "Open Cancel"),
                        ep("DOWNLOAD", "/api/bookings/{id}/ticket", "Download a booking ticket PDF",
                                "/view/bookings/ticket", "Download Ticket"),
                        ep("DOWNLOAD", "/api/bookings/group-ticket",
                                "Download group booking ticket PDF",
                                "/view/bookings/group-ticket", "Download Group Ticket"),
                        ep("GET ALL", "/api/reviews", "List every review", "/view/reviews", "Open List"),
                        ep("POST", "/api/reviews", "Post a new review",
                                "/view/reviews/add", "Open Add Form")))
                .screens(List.of(
                        sc("Booking Flow (Trip List)", "/view/bookings", "bi-ticket-perforated"),
                        sc("Cancel Booking", "/view/bookings/cancel", "bi-x-circle"),
                        sc("Booking Ticket Download", "/view/bookings/ticket", "bi-file-earmark-pdf"),
                        sc("Group Booking Ticket", "/view/bookings/group-ticket", "bi-files"),
                        sc("Reviews List", "/view/reviews", "bi-star-fill"),
                        sc("Add Review", "/view/reviews/add", "bi-star")))
                .build();
    }

    private List<TeamMember> buildMembers() {
        return List.of(sarthakPawar(), atharvKadam(), atharvaPawar(), anushkaBankar(), kedarMahadik());
    }
}
