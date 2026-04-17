package com.busticketbookingsystem.web.members;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * In-memory registry describing the 5 team members, their owned services,
 * and every operation each service exposes. Operations carry enough metadata
 * (DTO field definitions) for the UI to render dynamic forms that map 1:1
 * to the existing backend REST endpoints.
 */
@Component
public class MemberRegistry {

    private final List<Member> members = buildMembers();

    public List<Member> getAll() {
        return members;
    }

    public Optional<Member> findById(Integer id) {
        return members.stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    public Optional<Operation> findOperation(Integer memberId, String serviceKey, String operationName) {
        return findById(memberId).flatMap(m -> m.getServices().stream()
                .filter(s -> s.getKey().equalsIgnoreCase(serviceKey))
                .findFirst()
                .flatMap(s -> s.getOperations().stream()
                        .filter(o -> o.getName().equalsIgnoreCase(operationName))
                        .findFirst()));
    }

    // ---------- Field builders ----------

    private static FieldDef f(String name, String label, String type, String placeholder, boolean required, String valueType) {
        return FieldDef.builder().name(name).label(label).type(type)
                .placeholder(placeholder).required(required).valueType(valueType).build();
    }

    // ---------- DTO field definitions ----------

    private static final List<FieldDef> CUSTOMER_FIELDS = List.of(
            f("name", "Full Name", "text", "Sarthak Pawar", true, "string"),
            f("email", "Email", "email", "sarthak@example.com", true, "string"),
            f("phone", "Phone (10 digits)", "tel", "9876543210", true, "string"),
            f("addressId", "Address ID", "number", "1", true, "integer"));

    private static final List<FieldDef> ADDRESS_FIELDS = List.of(
            f("address", "Street Address", "text", "Station Road", true, "string"),
            f("city", "City", "text", "Mumbai", true, "string"),
            f("state", "State", "text", "Maharashtra", true, "string"),
            f("zipCode", "Zip Code (6 digits)", "text", "400001", true, "string"));

    private static final List<FieldDef> AGENCY_FIELDS = List.of(
            f("name", "Agency Name", "text", "Red Bus Agency", true, "string"),
            f("contactPersonName", "Contact Person Name", "text", "Sarthak", true, "string"),
            f("email", "Email", "email", "red@bus.com", true, "string"),
            f("phone", "Phone (10 digits)", "tel", "9876543210", true, "string"));

    private static final List<FieldDef> OFFICE_FIELDS = List.of(
            f("agencyId", "Agency ID", "number", "1", true, "integer"),
            f("officeMail", "Office Email", "email", "mumbai@red.com", true, "string"),
            f("officeContactPersonName", "Office Contact Person", "text", "Amit", true, "string"),
            f("officeContactNumber", "Office Phone (10 digits)", "tel", "9999999999", true, "string"),
            f("officeAddressId", "Office Address ID", "number", "1", true, "integer"));

    private static final List<FieldDef> BUS_FIELDS = List.of(
            f("officeId", "Office ID", "number", "1", true, "integer"),
            f("registrationNumber", "Registration Number", "text", "MH01AB1234", true, "string"),
            f("capacity", "Capacity (min 10)", "number", "40", true, "integer"),
            f("type", "Bus Type", "text", "AC Sleeper", true, "string"));

    private static final List<FieldDef> DRIVER_FIELDS = List.of(
            f("licenseNumber", "License Number", "text", "DL001", true, "string"),
            f("name", "Driver Name", "text", "Ramesh", true, "string"),
            f("phone", "Phone (10 digits)", "tel", "7777777777", true, "string"),
            f("officeId", "Office ID", "number", "1", true, "integer"),
            f("addressId", "Address ID", "number", "1", true, "integer"));

    private static final List<FieldDef> ROUTE_FIELDS = List.of(
            f("fromCity", "From City", "text", "Mumbai", true, "string"),
            f("toCity", "To City", "text", "Pune", true, "string"),
            f("breakPoints", "Break Points", "number", "1", false, "integer"),
            f("duration", "Duration (minutes)", "number", "180", false, "integer"));

    private static final List<FieldDef> TRIP_FIELDS = List.of(
            f("routeId", "Route ID", "number", "1", true, "integer"),
            f("busId", "Bus ID", "number", "1", true, "integer"),
            f("boardingAddressId", "Boarding Address ID", "number", "1", true, "integer"),
            f("droppingAddressId", "Dropping Address ID", "number", "2", true, "integer"),
            f("departureTime", "Departure Time", "datetime-local", "", true, "string"),
            f("arrivalTime", "Arrival Time", "datetime-local", "", true, "string"),
            f("driver1Id", "Driver 1 ID", "number", "1", true, "integer"),
            f("driver2Id", "Driver 2 ID (optional)", "number", "2", false, "integer"),
            f("availableSeats", "Available Seats", "number", "40", true, "integer"),
            f("fare", "Fare (Rs.)", "number", "500", true, "number"),
            f("tripDate", "Trip Date", "datetime-local", "", true, "string"));

    private static final List<FieldDef> PAYMENT_FIELDS = List.of(
            f("bookingId", "Booking ID", "number", "1", true, "integer"),
            f("customerId", "Customer ID", "number", "10", true, "integer"),
            f("amount", "Amount (Rs.)", "number", "500", true, "number"));

    private static final List<FieldDef> REVIEW_FIELDS = List.of(
            f("customerId", "Customer ID", "number", "1", true, "integer"),
            f("tripId", "Trip ID", "number", "1", true, "integer"),
            f("rating", "Rating (1-5)", "number", "5", true, "integer"),
            f("comment", "Comment", "textarea", "Great experience!", false, "string"));

    private static final List<FieldDef> BOOKING_FIELDS = List.of(
            f("tripId", "Trip ID", "number", "1", true, "integer"),
            f("seatNumbers", "Seat Numbers (comma-separated)", "text", "1,2,3", true, "integer-list"),
            f("customerId", "Customer ID", "number", "10", true, "integer"));

    // ---------- CRUD builders ----------

    private static Operation op(String name, String method, String endpoint, String inputKind, String desc, List<FieldDef> fields) {
        return Operation.builder().name(name).method(method).endpoint(endpoint)
                .inputKind(inputKind).description(desc).fields(fields).build();
    }

    private static List<Operation> crudOps(String base, List<FieldDef> fields) {
        return List.of(
                op("Get All", "GET", base, "NONE", "Fetch all records", List.of()),
                op("Get By ID", "GET", base + "/{id}", "ID", "Fetch one record by ID", List.of()),
                op("Create", "POST", base, "BODY", "Create a new record", fields),
                op("Update", "PUT", base + "/{id}", "ID_AND_BODY", "Update an existing record", fields)
        );
    }

    // ---------- Service builders ----------

    private static ServiceInfo customerService() {
        return ServiceInfo.builder().key("customers").name("Customers").icon("bi-people-fill")
                .description("Manage customer profiles and contact details")
                .operations(crudOps("/api/customers", CUSTOMER_FIELDS)).build();
    }

    private static ServiceInfo addressService() {
        return ServiceInfo.builder().key("addresses").name("Addresses").icon("bi-geo-alt-fill")
                .description("Manage addresses used by customers, offices, and trips")
                .operations(crudOps("/api/addresses", ADDRESS_FIELDS)).build();
    }

    private static ServiceInfo agencyService() {
        return ServiceInfo.builder().key("agencies").name("Agencies").icon("bi-building")
                .description("Manage bus agencies and their offices")
                .operations(crudOps("/api/agencies", AGENCY_FIELDS)).build();
    }

    private static ServiceInfo officeService() {
        return ServiceInfo.builder().key("offices").name("Agency Offices").icon("bi-shop")
                .description("Manage office branches linked to agencies")
                .operations(crudOps("/api/offices", OFFICE_FIELDS)).build();
    }

    private static ServiceInfo busService() {
        return ServiceInfo.builder().key("buses").name("Buses").icon("bi-bus-front-fill")
                .description("Manage physical buses with capacity and registration")
                .operations(crudOps("/api/buses", BUS_FIELDS)).build();
    }

    private static ServiceInfo driverService() {
        return ServiceInfo.builder().key("drivers").name("Drivers").icon("bi-person-badge")
                .description("Manage licensed drivers")
                .operations(crudOps("/api/drivers", DRIVER_FIELDS)).build();
    }

    private static ServiceInfo routeService() {
        return ServiceInfo.builder().key("routes").name("Routes").icon("bi-signpost-split")
                .description("Manage from-city -> to-city routes")
                .operations(crudOps("/api/routes", ROUTE_FIELDS)).build();
    }

    private static ServiceInfo tripService() {
        return ServiceInfo.builder().key("trips").name("Trips").icon("bi-calendar-event")
                .description("Manage scheduled trips with buses, drivers, fare")
                .operations(crudOps("/api/trips", TRIP_FIELDS)).build();
    }

    private static ServiceInfo paymentService() {
        List<Operation> ops = new java.util.ArrayList<>(List.of(
                op("Get All", "GET", "/api/payments", "NONE", "Fetch all payments", List.of()),
                op("Get By ID", "GET", "/api/payments/{id}", "ID", "Fetch payment by ID", List.of()),
                op("Create", "POST", "/api/payments", "BODY", "Process a new payment", PAYMENT_FIELDS)
        ));
        return ServiceInfo.builder().key("payments").name("Payments").icon("bi-credit-card")
                .description("Process payments and lookups")
                .operations(ops).build();
    }

    private static ServiceInfo reviewService() {
        List<Operation> ops = new java.util.ArrayList<>(List.of(
                op("Get All", "GET", "/api/reviews", "NONE", "Fetch all reviews", List.of()),
                op("Get By Trip", "GET", "/api/reviews/trip/{id}", "ID", "Reviews for a trip", List.of()),
                op("Get By Customer", "GET", "/api/reviews/customer/{id}", "ID", "Reviews by a customer", List.of()),
                op("Create", "POST", "/api/reviews", "BODY", "Post a new review", REVIEW_FIELDS)
        ));
        return ServiceInfo.builder().key("reviews").name("Reviews").icon("bi-star-fill")
                .description("Customer feedback and ratings for trips")
                .operations(ops).build();
    }

    private static ServiceInfo bookingService() {
        List<Operation> ops = new java.util.ArrayList<>(List.of(
                op("Get By Trip", "GET", "/api/bookings/trip/{id}", "ID", "All bookings for a trip", List.of()),
                op("Get By ID", "GET", "/api/bookings/{id}", "ID", "Fetch one booking by ID", List.of()),
                op("Create", "POST", "/api/bookings", "BODY", "Book one or more seats", BOOKING_FIELDS)
        ));
        return ServiceInfo.builder().key("bookings").name("Bookings").icon("bi-ticket-perforated")
                .description("Reserve seats and view booking status")
                .operations(ops).build();
    }

    private static ServiceInfo pdfService() {
        List<Operation> ops = List.of(
                op("Download Booking Ticket", "GET", "/api/bookings/{id}/ticket", "PDF_DOWNLOAD",
                        "Generate a boarding-pass PDF for a booking", List.of()),
                op("Download Payment Ticket", "GET", "/api/payments/{id}/ticket", "PDF_DOWNLOAD",
                        "Generate a boarding-pass PDF for a payment", List.of())
        );
        return ServiceInfo.builder().key("pdf").name("PDF Service").icon("bi-file-earmark-pdf-fill")
                .description("Generate downloadable boarding-pass PDFs")
                .operations(ops).build();
    }

    // ---------- Team ----------

    private List<Member> buildMembers() {
        return List.of(
                Member.builder().id(1).name("Member 1").role("Customer & Address Module")
                        .initials("M1").color("#0d6efd")
                        .image("https://api.dicebear.com/7.x/initials/svg?seed=M1&backgroundColor=0d6efd")
                        .summary("Customers, Addresses")
                        .services(List.of(customerService(), addressService())).build(),

                Member.builder().id(2).name("Member 2").role("Agency, Bus & Driver Module")
                        .initials("M2").color("#198754")
                        .image("https://api.dicebear.com/7.x/initials/svg?seed=M2&backgroundColor=198754")
                        .summary("Agencies, Offices, Buses, Drivers")
                        .services(List.of(agencyService(), officeService(), busService(), driverService())).build(),

                Member.builder().id(3).name("Member 3").role("Route & Trip Module")
                        .initials("M3").color("#fd7e14")
                        .image("https://api.dicebear.com/7.x/initials/svg?seed=M3&backgroundColor=fd7e14")
                        .summary("Routes, Trips")
                        .services(List.of(routeService(), tripService())).build(),

                Member.builder().id(4).name("Member 4").role("Payment & Review Module")
                        .initials("M4").color("#6f42c1")
                        .image("https://api.dicebear.com/7.x/initials/svg?seed=M4&backgroundColor=6f42c1")
                        .summary("Payments, Reviews")
                        .services(List.of(paymentService(), reviewService())).build(),

                Member.builder().id(5).name("Member 5").role("Booking & PDF Module")
                        .initials("M5").color("#dc3545")
                        .image("https://api.dicebear.com/7.x/initials/svg?seed=M5&backgroundColor=dc3545")
                        .summary("Bookings, PDF Service")
                        .services(List.of(bookingService(), pdfService())).build()
        );
    }
}
