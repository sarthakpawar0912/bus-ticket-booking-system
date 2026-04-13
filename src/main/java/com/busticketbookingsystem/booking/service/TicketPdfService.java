package com.busticketbookingsystem.booking.service;

import com.busticketbookingsystem.booking.entity.Booking;
import com.busticketbookingsystem.booking.repository.BookingRepository;
import com.busticketbookingsystem.exception.ResourceNotFoundException;
import com.busticketbookingsystem.payment.entity.Payment;
import com.busticketbookingsystem.payment.repository.PaymentRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.DashedBorder;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class TicketPdfService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    private static final DeviceRgb NAVY = new DeviceRgb(12, 35, 75);
    private static final DeviceRgb NAVY_MED = new DeviceRgb(20, 50, 105);
    private static final DeviceRgb NAVY_LIGHT = new DeviceRgb(35, 70, 140);
    private static final DeviceRgb TEAL = new DeviceRgb(0, 172, 156);
    private static final DeviceRgb TEAL_DARK = new DeviceRgb(0, 130, 118);
    private static final DeviceRgb TEAL_BG = new DeviceRgb(232, 248, 245);
    private static final DeviceRgb ORANGE = new DeviceRgb(255, 152, 0);
    private static final DeviceRgb GOLD = new DeviceRgb(255, 193, 7);
    private static final DeviceRgb BG = new DeviceRgb(248, 249, 252);
    private static final DeviceRgb CARD_BG = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb GRAY = new DeviceRgb(140, 150, 165);
    private static final DeviceRgb DARK = new DeviceRgb(30, 34, 42);
    private static final DeviceRgb GREEN = new DeviceRgb(16, 150, 72);
    private static final DeviceRgb GREEN_BG = new DeviceRgb(220, 245, 230);
    private static final DeviceRgb RED_BG = new DeviceRgb(255, 235, 230);
    private static final DeviceRgb RED = new DeviceRgb(200, 40, 40);
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb SUBTLE = new DeviceRgb(220, 225, 235);

    private static final String DOTS = "...............";

    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter T_FMT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @Transactional(readOnly = true)
    public byte[] generateGroupBookingTicket(java.util.List<Integer> bookingIds) {
        java.util.List<Payment> fakePayments = new java.util.ArrayList<>();
        for (Integer bid : bookingIds) {
            Booking booking = bookingRepository.findByIdWithTripDetails(bid).orElse(null);
            if (booking == null) booking = bookingRepository.findById(bid).orElse(null);
            if (booking != null) {
                // Check if a real payment exists
                Payment realPayment = paymentRepository.findByBooking_BookingId(bid).orElse(null);
                if (realPayment != null) {
                    fakePayments.add(realPayment);
                } else {
                    // Build a shell payment just to carry booking + trip data into the PDF
                    Payment shell = Payment.builder()
                            .paymentId(0)
                            .booking(booking)
                            .amount(booking.getTrip() != null ? booking.getTrip().getFare() : java.math.BigDecimal.ZERO)
                            .build();
                    fakePayments.add(shell);
                }
            }
        }
        if (fakePayments.isEmpty()) {
            throw new ResourceNotFoundException("No bookings found for the given IDs");
        }
        return buildGroupTicketPdf(fakePayments);
    }

    @Transactional(readOnly = true)
    public byte[] generateGroupTicket(java.util.List<Integer> paymentIds) {
        java.util.List<Payment> payments = new java.util.ArrayList<>();
        for (Integer pid : paymentIds) {
            paymentRepository.findByIdWithAllDetails(pid).ifPresent(payments::add);
        }
        if (payments.isEmpty()) {
            throw new ResourceNotFoundException("No payments found for the given IDs");
        }
        return buildGroupTicketPdf(payments);
    }

    @Transactional(readOnly = true)
    public byte[] generateTicketByPaymentId(Integer paymentId) {
        Payment payment = paymentRepository.findByIdWithAllDetails(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        Booking booking = payment.getBooking();
        return buildTicketPdf(booking, payment);
    }

    @Transactional(readOnly = true)
    public byte[] generateTicketPdf(Integer bookingId) {
        Booking booking = bookingRepository.findByIdWithTripDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        Payment payment = paymentRepository.findByBooking_BookingId(bookingId).orElse(null);
        return buildTicketPdf(booking, payment);
    }

    @SuppressWarnings("java:S3776")
    private byte[] buildTicketPdf(Booking booking, Payment payment) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);
            Document doc = new Document(pdfDoc);
            doc.setMargins(20, 25, 20, 25);

            boolean hasBooking = booking != null;
            boolean hasTrip = hasBooking && booking.getTrip() != null;
            boolean hasRoute = hasTrip && booking.getTrip().getRoute() != null;
            boolean hasBus = hasTrip && booking.getTrip().getBus() != null;
            boolean hasBoard = hasTrip && booking.getTrip().getBoardingAddress() != null;
            boolean hasDrop = hasTrip && booking.getTrip().getDroppingAddress() != null;

            String from = hasRoute ? booking.getTrip().getRoute().getFromCity() : "N/A";
            String to = hasRoute ? booking.getTrip().getRoute().getToCity() : "N/A";

            // ━━━━━━━━━━━━ DRAW PAGE BACKGROUND ━━━━━━━━━━━━
            pdfDoc.addNewPage();
            PdfCanvas canvas = new PdfCanvas(pdfDoc.getFirstPage());
            // Full page light background
            canvas.saveState()
                    .setFillColor(BG)
                    .rectangle(0, 0, PageSize.A4.getWidth(), PageSize.A4.getHeight())
                    .fill()
                    .restoreState();
            // Top navy bar (decorative)
            canvas.saveState()
                    .setFillColor(NAVY)
                    .rectangle(0, PageSize.A4.getHeight() - 8, PageSize.A4.getWidth(), 8)
                    .fill()
                    .restoreState();
            // Bottom teal accent bar
            canvas.saveState()
                    .setFillColor(TEAL)
                    .rectangle(0, 0, PageSize.A4.getWidth(), 4)
                    .fill()
                    .restoreState();

            // ━━━━━━━━━━━━ HEADER ━━━━━━━━━━━━
            Table hdr = fullWidth(new float[]{1, 1, 1});
            hdr.setBackgroundColor(NAVY).setMarginBottom(0);

            Cell brand = noBorderCell().setPadding(12).setPaddingLeft(16);
            brand.add(p("BUS TICKET BOOKING", 11, WHITE, true));
            brand.add(p("SYSTEM", 11, WHITE, true).setMarginTop(-2));
            hdr.addCell(brand);

            Cell center = noBorderCell().setPadding(12).setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);
            center.add(p("E - T I C K E T", 7, SUBTLE, false).setCharacterSpacing(2));
            center.add(p("BOARDING PASS", 8, GOLD, true).setCharacterSpacing(1.5f).setMarginTop(1));
            hdr.addCell(center);

            Cell tid = noBorderCell().setPadding(12).setPaddingRight(16)
                    .setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE);
            tid.add(p("TICKET", 6.5f, SUBTLE, false));
            int ticketId = 0;
            if (hasBooking) {
                ticketId = booking.getBookingId();
            } else if (payment != null) {
                ticketId = payment.getPaymentId();
            }
            tid.add(p("#" + String.format("%06d", ticketId), 16, WHITE, true).setMarginTop(0));
            hdr.addCell(tid);
            doc.add(hdr);

            // ━━━━━━━━━━━━ QUICK INFO STRIP ━━━━━━━━━━━━
            Table strip = fullWidth(new float[]{1, 1, 1, 1, 1});
            strip.setBackgroundColor(NAVY_MED).setMarginBottom(0);
            strip.addCell(chipCell("BOOKING", hasBooking ? String.valueOf(booking.getBookingId()) : "N/A"));
            strip.addCell(chipCell("SEAT", hasBooking ? String.valueOf(booking.getSeatNumber()) : "N/A"));
            strip.addCell(chipCell("STATUS", hasBooking && booking.getStatus() != null ? booking.getStatus().name() : "N/A"));
            strip.addCell(chipCell("DATE", hasTrip && booking.getTrip().getTripDate() != null
                    ? booking.getTrip().getTripDate().format(D_FMT) : "N/A"));
            strip.addCell(chipCell("FARE", hasTrip && booking.getTrip().getFare() != null
                    ? "Rs." + booking.getTrip().getFare().toPlainString() : "N/A"));
            doc.add(strip);

            // ━━━━━━━━━━━━ ROUTE HERO ━━━━━━━━━━━━
            Table route = fullWidth(new float[]{5, 3, 5});
            route.setBackgroundColor(CARD_BG).setMarginTop(8);
            route.setBorder(new SolidBorder(SUBTLE, 0.5f));

            // FROM
            Cell fc = noBorderCell().setPadding(10).setTextAlignment(TextAlignment.CENTER);
            fc.add(p("DEPARTURE", 6.5f, GRAY, true));
            fc.add(p(from.toUpperCase(), 20, NAVY, true).setMarginTop(2));
            if (hasTrip && booking.getTrip().getDepartureTime() != null) {
                fc.add(p(booking.getTrip().getDepartureTime().format(T_FMT), 11, TEAL, true).setMarginTop(1));
                fc.add(p(booking.getTrip().getDepartureTime().format(D_FMT), 7.5f, GRAY, false));
            }
            route.addCell(fc);

            // CENTER ARROW
            Cell ac = noBorderCell().setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(6);
            ac.add(p(DOTS, 6, GRAY, false).setMarginBottom(0));
            ac.setBackgroundColor(BG);
            ac.add(p("BUS", 6, GRAY, true).setMarginTop(0));
            ac.add(p(">>>>>>", 12, TEAL, true).setMarginTop(-1).setCharacterSpacing(1));
            if (hasBus) {
                ac.add(p(booking.getTrip().getBus().getType(), 6.5f, NAVY_LIGHT, true).setMarginTop(1));
            }
            ac.add(p(DOTS, 6, GRAY, false).setMarginTop(0));
            route.addCell(ac);

            // TO
            Cell tc = noBorderCell().setPadding(10).setTextAlignment(TextAlignment.CENTER);
            tc.add(p("ARRIVAL", 6.5f, GRAY, true));
            tc.add(p(to.toUpperCase(), 20, NAVY, true).setMarginTop(2));
            if (hasTrip && booking.getTrip().getArrivalTime() != null) {
                tc.add(p(booking.getTrip().getArrivalTime().format(T_FMT), 11, TEAL, true).setMarginTop(1));
                tc.add(p(booking.getTrip().getArrivalTime().format(D_FMT), 7.5f, GRAY, false));
            }
            route.addCell(tc);
            doc.add(route);

            // ━━━━━━━━━━━━ TEAR LINE ━━━━━━━━━━━━
            Table tear = fullWidth(new float[]{1, 3, 1});
            tear.setMarginTop(6).setMarginBottom(2);
            tear.addCell(noBorderCell().setBorderBottom(new DashedBorder(GRAY, 0.5f)));
            Cell tearMid = noBorderCell().setTextAlignment(TextAlignment.CENTER).setPadding(2);
            tearMid.add(p("PASSENGER COPY", 6, GRAY, true).setCharacterSpacing(2));
            tear.addCell(tearMid);
            tear.addCell(noBorderCell().setBorderBottom(new DashedBorder(GRAY, 0.5f)));
            doc.add(tear);

            // ━━━━━━━━━━━━ PASSENGER (if available) ━━━━━━━━━━━━
            if (payment != null && payment.getCustomer() != null) {
                doc.add(sec("PASSENGER DETAILS"));
                Table pt = fullWidth(new float[]{2, 2, 1});
                pt.setBackgroundColor(CARD_BG).setBorder(new SolidBorder(SUBTLE, 0.5f));
                pt.addCell(dataCell("Passenger Name", payment.getCustomer().getName(), true));
                pt.addCell(dataCell("Email Address", payment.getCustomer().getEmail(), false));
                pt.addCell(dataCell("Phone", payment.getCustomer().getPhone(), false));
                doc.add(pt);
            }

            // ━━━━━━━━━━━━ BUS + JOURNEY COMBINED ━━━━━━━━━━━━
            doc.add(sec("JOURNEY & BUS DETAILS"));
            Table jb = fullWidth(new float[]{1, 1, 1, 1, 1});
            jb.setBackgroundColor(CARD_BG).setBorder(new SolidBorder(SUBTLE, 0.5f));
            jb.addCell(dataCell("Departure", hasTrip && booking.getTrip().getDepartureTime() != null
                    ? booking.getTrip().getDepartureTime().format(DT_FMT) : "N/A", true));
            jb.addCell(dataCell("Arrival", hasTrip && booking.getTrip().getArrivalTime() != null
                    ? booking.getTrip().getArrivalTime().format(DT_FMT) : "N/A", true));
            jb.addCell(dataCell("Seat No.", hasBooking ? "Seat " + booking.getSeatNumber() : "N/A", true));
            jb.addCell(dataCell("Bus Reg. No.", hasBus ? booking.getTrip().getBus().getRegistrationNumber() : "N/A", false));
            jb.addCell(dataCell("Capacity", hasBus ? booking.getTrip().getBus().getCapacity() + " Seats" : "N/A", false));
            doc.add(jb);

            // ━━━━━━━━━━━━ BOARDING & DROPPING ━━━━━━━━━━━━
            doc.add(sec("BOARDING & DROPPING POINTS"));
            Table bd = fullWidth(new float[]{1, 1});
            bd.setMarginBottom(0);

            // Boarding
            Cell bc = noBorderCell().setPadding(10).setBackgroundColor(TEAL_BG)
                    .setBorderLeft(new SolidBorder(TEAL, 3));
            bc.add(p("BOARDING POINT", 6.5f, TEAL_DARK, true).setCharacterSpacing(0.5f));
            if (hasBoard) {
                bc.add(p(booking.getTrip().getBoardingAddress().getAddress(), 9, DARK, true).setMarginTop(2));
                bc.add(p(booking.getTrip().getBoardingAddress().getCity() + ", "
                                + booking.getTrip().getBoardingAddress().getState() + " - "
                                + booking.getTrip().getBoardingAddress().getZipCode(),
                        7.5f, GRAY, false).setMarginTop(1));
            } else {
                bc.add(p("N/A", 9, DARK, false).setMarginTop(2));
            }
            bd.addCell(bc);

            // Dropping
            Cell dc = noBorderCell().setPadding(10).setBackgroundColor(BG)
                    .setBorderLeft(new SolidBorder(ORANGE, 3));
            dc.add(p("DROPPING POINT", 6.5f, ORANGE, true).setCharacterSpacing(0.5f));
            if (hasDrop) {
                dc.add(p(booking.getTrip().getDroppingAddress().getAddress(), 9, DARK, true).setMarginTop(2));
                dc.add(p(booking.getTrip().getDroppingAddress().getCity() + ", "
                                + booking.getTrip().getDroppingAddress().getState() + " - "
                                + booking.getTrip().getDroppingAddress().getZipCode(),
                        7.5f, GRAY, false).setMarginTop(1));
            } else {
                dc.add(p("N/A", 9, DARK, false).setMarginTop(2));
            }
            bd.addCell(dc);
            doc.add(bd);

            // ━━━━━━━━━━━━ PAYMENT ━━━━━━━━━━━━
            doc.add(sec("PAYMENT"));
            if (payment != null) {
                Table pay = fullWidth(new float[]{1, 1, 1, 1});
                pay.setBackgroundColor(GREEN_BG).setBorder(new SolidBorder(new DeviceRgb(180, 230, 200), 0.5f));
                pay.addCell(dataCell("Payment ID", String.valueOf(payment.getPaymentId()), false));
                pay.addCell(dataCellColored("Amount Paid", "Rs. " + payment.getAmount().toPlainString(), GREEN, true));
                pay.addCell(dataCellColored("Status", payment.getPaymentStatus().name().toUpperCase(), GREEN, true));
                pay.addCell(dataCell("Date", payment.getPaymentDate() != null
                        ? payment.getPaymentDate().format(DT_FMT) : "N/A", false));
                doc.add(pay);
            } else {
                Table pend = fullWidth(new float[]{1});
                Cell pc = noBorderCell().setPadding(8).setBackgroundColor(RED_BG).setTextAlignment(TextAlignment.CENTER);
                pc.add(p("PAYMENT PENDING - Please complete payment to confirm booking", 8, RED, true));
                pend.addCell(pc);
                doc.add(pend);
            }

            // ━━━━━━━━━━━━ REFERENCE BARCODE STRIP ━━━━━━━━━━━━
            int paymentIdOrZero = payment != null ? payment.getPaymentId() : 0;
            int refId = hasBooking ? booking.getBookingId() : paymentIdOrZero;
            int refSeat = hasBooking ? booking.getSeatNumber() : 0;
            String ref = "TXN" + String.format("%06d", refId)
                    + "S" + String.format("%02d", refSeat)
                    + (hasTrip && booking.getTrip().getTripDate() != null
                    ? booking.getTrip().getTripDate().format(DateTimeFormatter.ofPattern("ddMMyy")) : "000000");

            Table bar = fullWidth(new float[]{4, 2});
            bar.setBackgroundColor(NAVY).setMarginTop(8);

            Cell rc = noBorderCell().setPadding(8).setPaddingLeft(14);
            rc.add(p("TRANSACTION REFERENCE", 5.5f, SUBTLE, true).setCharacterSpacing(1));
            rc.add(p(ref, 12, WHITE, true).setCharacterSpacing(2.5f).setMarginTop(1));
            bar.addCell(rc);

            Cell brc = noBorderCell().setPadding(8).setTextAlignment(TextAlignment.RIGHT)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE).setPaddingRight(14);
            // Pseudo barcode
            StringBuilder bars = new StringBuilder();
            int h = ref.hashCode();
            for (int i = 0; i < 35; i++) bars.append(((h >> (i % 16)) & 1) == 0 ? "|" : "l");
            brc.add(p(bars.toString(), 16, WHITE, false).setCharacterSpacing(0.8f));
            bar.addCell(brc);
            doc.add(bar);

            // Thin teal accent line below barcode
            Table accentLine = fullWidth(new float[]{1});
            Cell alc = noBorderCell().setBackgroundColor(TEAL).setHeight(3);
            alc.add(p("", 1, TEAL, false));
            accentLine.addCell(alc);
            doc.add(accentLine);

            // ━━━━━━━━━━━━ FOOTER ━━━━━━━━━━━━
            Table ftr = fullWidth(new float[]{3, 2});
            ftr.setMarginTop(6);

            Cell fl = noBorderCell().setPadding(4);
            fl.add(p("IMPORTANT:", 6.5f, DARK, true));
            fl.add(p("Arrive 15 min early  |  Carry valid photo ID  |  Cancellation charges may apply", 6, GRAY, false).setMarginTop(1));
            ftr.addCell(fl);

            Cell fr = noBorderCell().setPadding(4).setTextAlignment(TextAlignment.RIGHT);
            fr.add(p("Bus Ticket Booking System", 7, NAVY, true));
            fr.add(p("Generated: " + LocalDateTime.now().format(DT_FMT), 5.5f, GRAY, false));
            fr.add(p("Computer-generated ticket. No signature required.", 5, GRAY, false).setItalic());
            ftr.addCell(fr);
            doc.add(ftr);

            doc.close();
            return baos.toByteArray();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException | java.io.IOException e) {
            throw new com.busticketbookingsystem.exception.BadRequestException("Failed to generate PDF: " + e.getMessage());
        }
    }

    @SuppressWarnings({"java:S3776", "java:S1541", "java:S6541"})
    private byte[] buildGroupTicketPdf(java.util.List<Payment> payments) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);
            Document doc = new Document(pdfDoc);
            doc.setMargins(20, 25, 20, 25);

            // Gather data from first payment for common info
            Payment firstPayment = payments.get(0);
            Booking firstBooking = firstPayment.getBooking();
            boolean hasBooking = firstBooking != null;
            boolean hasTrip = hasBooking && firstBooking.getTrip() != null;
            boolean hasRoute = hasTrip && firstBooking.getTrip().getRoute() != null;
            boolean hasBus = hasTrip && firstBooking.getTrip().getBus() != null;
            boolean hasBoard = hasTrip && firstBooking.getTrip().getBoardingAddress() != null;
            boolean hasDrop = hasTrip && firstBooking.getTrip().getDroppingAddress() != null;

            String from = hasRoute ? firstBooking.getTrip().getRoute().getFromCity() : "N/A";
            String to = hasRoute ? firstBooking.getTrip().getRoute().getToCity() : "N/A";

            // Collect all seat numbers and total amount
            StringBuilder seatStr = new StringBuilder();
            java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
            for (Payment p : payments) {
                totalAmount = totalAmount.add(p.getAmount() != null ? p.getAmount() : java.math.BigDecimal.ZERO);
                if (p.getBooking() != null) {
                    if (!seatStr.isEmpty()) seatStr.append(", ");
                    seatStr.append(p.getBooking().getSeatNumber());
                }
            }
            String allSeats = !seatStr.isEmpty() ? seatStr.toString() : "N/A";

            // Background
            pdfDoc.addNewPage();
            PdfCanvas canvas = new PdfCanvas(pdfDoc.getFirstPage());
            canvas.saveState().setFillColor(BG)
                    .rectangle(0, 0, PageSize.A4.getWidth(), PageSize.A4.getHeight()).fill().restoreState();
            canvas.saveState().setFillColor(NAVY)
                    .rectangle(0, PageSize.A4.getHeight() - 8, PageSize.A4.getWidth(), 8).fill().restoreState();
            canvas.saveState().setFillColor(TEAL)
                    .rectangle(0, 0, PageSize.A4.getWidth(), 4).fill().restoreState();

            // ━━━ HEADER ━━━
            Table hdr = fullWidth(new float[]{1, 1, 1});
            hdr.setBackgroundColor(NAVY).setMarginBottom(0);
            Cell brand = noBorderCell().setPadding(12).setPaddingLeft(16);
            brand.add(p("BUS TICKET BOOKING", 11, WHITE, true));
            brand.add(p("SYSTEM", 11, WHITE, true).setMarginTop(-2));
            hdr.addCell(brand);
            Cell center = noBorderCell().setPadding(12).setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);
            center.add(p("G R O U P", 7, SUBTLE, false).setCharacterSpacing(2));
            center.add(p("BOARDING PASS", 8, GOLD, true).setCharacterSpacing(1.5f).setMarginTop(1));
            hdr.addCell(center);
            Cell tid = noBorderCell().setPadding(12).setPaddingRight(16)
                    .setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE);
            tid.add(p(payments.size() + " SEATS", 7, SUBTLE, false));
            tid.add(p("#" + String.format("%06d", firstPayment.getPaymentId()), 16, WHITE, true).setMarginTop(0));
            hdr.addCell(tid);
            doc.add(hdr);

            // ━━━ QUICK INFO STRIP ━━━
            Table strip = fullWidth(new float[]{1, 1, 1, 1});
            strip.setBackgroundColor(NAVY_MED).setMarginBottom(0);
            strip.addCell(chipCell("SEATS", allSeats));
            strip.addCell(chipCell("PASSENGERS", String.valueOf(payments.size())));
            strip.addCell(chipCell("DATE", hasTrip && firstBooking.getTrip().getTripDate() != null
                    ? firstBooking.getTrip().getTripDate().format(D_FMT) : "N/A"));
            strip.addCell(chipCell("TOTAL FARE", "Rs." + totalAmount.toPlainString()));
            doc.add(strip);

            // ━━━ ROUTE HERO ━━━
            Table route = fullWidth(new float[]{5, 3, 5});
            route.setBackgroundColor(CARD_BG).setMarginTop(8).setBorder(new SolidBorder(SUBTLE, 0.5f));
            Cell fc = noBorderCell().setPadding(10).setTextAlignment(TextAlignment.CENTER);
            fc.add(p("DEPARTURE", 6.5f, GRAY, true));
            fc.add(p(from.toUpperCase(), 20, NAVY, true).setMarginTop(2));
            if (hasTrip && firstBooking.getTrip().getDepartureTime() != null) {
                fc.add(p(firstBooking.getTrip().getDepartureTime().format(T_FMT), 11, TEAL, true).setMarginTop(1));
                fc.add(p(firstBooking.getTrip().getDepartureTime().format(D_FMT), 7.5f, GRAY, false));
            }
            route.addCell(fc);
            Cell ac = noBorderCell().setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(6).setBackgroundColor(BG);
            ac.add(p(DOTS, 6, GRAY, false));
            ac.add(p("BUS", 6, GRAY, true));
            ac.add(p(">>>>>>", 12, TEAL, true).setCharacterSpacing(1));
            if (hasBus) ac.add(p(firstBooking.getTrip().getBus().getType(), 6.5f, NAVY_LIGHT, true).setMarginTop(1));
            ac.add(p(DOTS, 6, GRAY, false));
            route.addCell(ac);
            Cell tc = noBorderCell().setPadding(10).setTextAlignment(TextAlignment.CENTER);
            tc.add(p("ARRIVAL", 6.5f, GRAY, true));
            tc.add(p(to.toUpperCase(), 20, NAVY, true).setMarginTop(2));
            if (hasTrip && firstBooking.getTrip().getArrivalTime() != null) {
                tc.add(p(firstBooking.getTrip().getArrivalTime().format(T_FMT), 11, TEAL, true).setMarginTop(1));
                tc.add(p(firstBooking.getTrip().getArrivalTime().format(D_FMT), 7.5f, GRAY, false));
            }
            route.addCell(tc);
            doc.add(route);

            // ━━━ TEAR LINE ━━━
            Table tear = fullWidth(new float[]{1, 3, 1});
            tear.setMarginTop(6).setMarginBottom(2);
            tear.addCell(noBorderCell().setBorderBottom(new DashedBorder(GRAY, 0.5f)));
            Cell tearMid = noBorderCell().setTextAlignment(TextAlignment.CENTER).setPadding(2);
            tearMid.add(p("GROUP TICKET - " + payments.size() + " PASSENGERS", 6, GRAY, true).setCharacterSpacing(2));
            tear.addCell(tearMid);
            tear.addCell(noBorderCell().setBorderBottom(new DashedBorder(GRAY, 0.5f)));
            doc.add(tear);

            // ━━━ PASSENGER ━━━
            if (firstPayment.getCustomer() != null) {
                doc.add(sec("PASSENGER DETAILS"));
                Table pt = fullWidth(new float[]{2, 2, 1});
                pt.setBackgroundColor(CARD_BG).setBorder(new SolidBorder(SUBTLE, 0.5f));
                pt.addCell(dataCell("Passenger Name", firstPayment.getCustomer().getName(), true));
                pt.addCell(dataCell("Email", firstPayment.getCustomer().getEmail(), false));
                pt.addCell(dataCell("Phone", firstPayment.getCustomer().getPhone(), false));
                doc.add(pt);
            }

            // ━━━ SEAT-WISE BREAKDOWN TABLE ━━━
            doc.add(sec("SEAT-WISE BOOKING DETAILS"));
            Table seatTable = fullWidth(new float[]{1, 1, 1, 1, 1});
            seatTable.setBackgroundColor(CARD_BG).setBorder(new SolidBorder(SUBTLE, 0.5f));
            // Header row
            seatTable.addCell(tableHeader("Seat No."));
            seatTable.addCell(tableHeader("Booking ID"));
            seatTable.addCell(tableHeader("Payment ID"));
            seatTable.addCell(tableHeader("Status"));
            seatTable.addCell(tableHeader("Amount"));
            // Data rows
            for (Payment pay : payments) {
                Booking b = pay.getBooking();
                seatTable.addCell(dataCell("", b != null ? "Seat " + b.getSeatNumber() : "N/A", true));
                seatTable.addCell(dataCell("", b != null ? String.valueOf(b.getBookingId()) : "N/A", false));
                seatTable.addCell(dataCell("", String.valueOf(pay.getPaymentId()), false));
                seatTable.addCell(dataCellColored("", pay.getPaymentStatus() != null ? pay.getPaymentStatus().name() : "N/A", GREEN, true));
                seatTable.addCell(dataCell("", pay.getAmount() != null ? "Rs." + pay.getAmount().toPlainString() : "N/A", false));
            }
            // Total row
            Cell totalLabel = noBorderCell().setPadding(7).setBackgroundColor(NAVY);
            totalLabel.add(p("TOTAL (" + payments.size() + " seats)", 8, WHITE, true));
            seatTable.addCell(totalLabel);
            seatTable.addCell(noBorderCell().setBackgroundColor(NAVY));
            seatTable.addCell(noBorderCell().setBackgroundColor(NAVY));
            seatTable.addCell(noBorderCell().setBackgroundColor(NAVY));
            Cell totalVal = noBorderCell().setPadding(7).setBackgroundColor(NAVY);
            totalVal.add(p("Rs." + totalAmount.toPlainString(), 9, WHITE, true));
            seatTable.addCell(totalVal);
            doc.add(seatTable);

            // ━━━ BUS DETAILS ━━━
            if (hasBus) {
                doc.add(sec("BUS DETAILS"));
                Table bTable = fullWidth(new float[]{1, 1, 1});
                bTable.setBackgroundColor(CARD_BG).setBorder(new SolidBorder(SUBTLE, 0.5f));
                bTable.addCell(dataCell("Bus Type", firstBooking.getTrip().getBus().getType(), false));
                bTable.addCell(dataCell("Registration", firstBooking.getTrip().getBus().getRegistrationNumber(), true));
                bTable.addCell(dataCell("Capacity", firstBooking.getTrip().getBus().getCapacity() + " Seats", false));
                doc.add(bTable);
            }

            // ━━━ BOARDING & DROPPING ━━━
            doc.add(sec("BOARDING & DROPPING POINTS"));
            Table bd = fullWidth(new float[]{1, 1});
            Cell bc = noBorderCell().setPadding(10).setBackgroundColor(TEAL_BG).setBorderLeft(new SolidBorder(TEAL, 3));
            bc.add(p("BOARDING POINT", 6.5f, TEAL_DARK, true));
            if (hasBoard) {
                bc.add(p(firstBooking.getTrip().getBoardingAddress().getAddress(), 9, DARK, true).setMarginTop(2));
                bc.add(p(firstBooking.getTrip().getBoardingAddress().getCity() + ", "
                        + firstBooking.getTrip().getBoardingAddress().getState() + " - "
                        + firstBooking.getTrip().getBoardingAddress().getZipCode(), 7.5f, GRAY, false).setMarginTop(1));
            } else { bc.add(p("N/A", 9, DARK, false).setMarginTop(2)); }
            bd.addCell(bc);
            Cell dc = noBorderCell().setPadding(10).setBackgroundColor(BG).setBorderLeft(new SolidBorder(ORANGE, 3));
            dc.add(p("DROPPING POINT", 6.5f, ORANGE, true));
            if (hasDrop) {
                dc.add(p(firstBooking.getTrip().getDroppingAddress().getAddress(), 9, DARK, true).setMarginTop(2));
                dc.add(p(firstBooking.getTrip().getDroppingAddress().getCity() + ", "
                        + firstBooking.getTrip().getDroppingAddress().getState() + " - "
                        + firstBooking.getTrip().getDroppingAddress().getZipCode(), 7.5f, GRAY, false).setMarginTop(1));
            } else { dc.add(p("N/A", 9, DARK, false).setMarginTop(2)); }
            bd.addCell(dc);
            doc.add(bd);

            // ━━━ BARCODE STRIP ━━━
            String ref = "GRP" + String.format("%06d", firstPayment.getPaymentId()) + "X" + payments.size()
                    + "S" + (hasTrip && firstBooking.getTrip().getTripDate() != null
                    ? firstBooking.getTrip().getTripDate().format(DateTimeFormatter.ofPattern("ddMMyy")) : "000000");
            Table bar = fullWidth(new float[]{4, 2});
            bar.setBackgroundColor(NAVY).setMarginTop(8);
            Cell rc = noBorderCell().setPadding(8).setPaddingLeft(14);
            rc.add(p("TRANSACTION REFERENCE", 5.5f, SUBTLE, true).setCharacterSpacing(1));
            rc.add(p(ref, 12, WHITE, true).setCharacterSpacing(2.5f).setMarginTop(1));
            bar.addCell(rc);
            Cell brc = noBorderCell().setPadding(8).setTextAlignment(TextAlignment.RIGHT)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE).setPaddingRight(14);
            StringBuilder bars = new StringBuilder();
            int h = ref.hashCode();
            for (int i = 0; i < 35; i++) bars.append(((h >> (i % 16)) & 1) == 0 ? "|" : "l");
            brc.add(p(bars.toString(), 16, WHITE, false).setCharacterSpacing(0.8f));
            bar.addCell(brc);
            doc.add(bar);
            Table accentLine = fullWidth(new float[]{1});
            Cell alc = noBorderCell().setBackgroundColor(TEAL).setHeight(3);
            alc.add(p("", 1, TEAL, false));
            accentLine.addCell(alc);
            doc.add(accentLine);

            // ━━━ FOOTER ━━━
            Table ftr = fullWidth(new float[]{3, 2});
            ftr.setMarginTop(6);
            Cell fl = noBorderCell().setPadding(4);
            fl.add(p("IMPORTANT:", 6.5f, DARK, true));
            fl.add(p("Arrive 15 min early  |  Carry valid photo ID  |  Cancellation charges may apply", 6, GRAY, false).setMarginTop(1));
            ftr.addCell(fl);
            Cell fr = noBorderCell().setPadding(4).setTextAlignment(TextAlignment.RIGHT);
            fr.add(p("Bus Ticket Booking System", 7, NAVY, true));
            fr.add(p("Generated: " + LocalDateTime.now().format(DT_FMT), 5.5f, GRAY, false));
            fr.add(p("Computer-generated ticket. No signature required.", 5, GRAY, false).setItalic());
            ftr.addCell(fr);
            doc.add(ftr);

            doc.close();
            return baos.toByteArray();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException | java.io.IOException e) {
            throw new com.busticketbookingsystem.exception.BadRequestException("Failed to generate PDF: " + e.getMessage());
        }
    }

    private Cell tableHeader(String text) {
        Cell c = noBorderCell().setPadding(6).setBackgroundColor(NAVY_LIGHT);
        c.add(p(text, 7, WHITE, true).setCharacterSpacing(0.3f));
        return c;
    }

    // ────────── Helper Methods ──────────

    private Table fullWidth(float[] cols) {
        return new Table(UnitValue.createPercentArray(cols)).useAllAvailableWidth();
    }

    private Cell noBorderCell() {
        return new Cell().setBorder(Border.NO_BORDER);
    }

    private Paragraph p(String text, float size, DeviceRgb color, boolean bold) {
        Paragraph para = new Paragraph(text).setFontSize(size).setFontColor(color).setMarginBottom(0).setMarginTop(0);
        if (bold) para.setBold();
        return para;
    }

    private Paragraph sec(String title) {
        return new Paragraph(title).setFontSize(7.5f).setBold().setFontColor(NAVY)
                .setCharacterSpacing(1.2f).setMarginTop(8).setMarginBottom(3)
                .setBorderBottom(new SolidBorder(TEAL, 1)).setPaddingBottom(2);
    }

    private Cell chipCell(String label, String value) {
        Cell c = noBorderCell().setPadding(5).setTextAlignment(TextAlignment.CENTER);
        c.add(p(label, 5.5f, SUBTLE, false).setCharacterSpacing(0.5f));
        c.add(p(value, 8.5f, WHITE, true).setMarginTop(1));
        return c;
    }

    private Cell dataCell(String label, String value, boolean bold) {
        Cell c = noBorderCell().setPadding(7);
        c.add(p(label, 6, GRAY, true).setCharacterSpacing(0.3f));
        Paragraph v = p(value != null ? value : "N/A", 8.5f, DARK, bold).setMarginTop(2);
        c.add(v);
        return c;
    }

    private Cell dataCellColored(String label, String value, DeviceRgb color, boolean bold) {
        Cell c = noBorderCell().setPadding(7);
        c.add(p(label, 6, GRAY, true).setCharacterSpacing(0.3f));
        c.add(p(value != null ? value : "N/A", 8.5f, color, bold).setMarginTop(2));
        return c;
    }
}
