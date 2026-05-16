package com.smartlibrary.model;

import java.time.LocalDate;

public class Reservation {
    private final int id;
    private int bookId;
    private int memberId;
    private String bookTitle;
    private String memberName;
    private LocalDate reservationDate;
    private String status;

    public Reservation(int id, int bookId, int memberId, String bookTitle, String memberName, LocalDate reservationDate, String status) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.bookTitle = bookTitle;
        this.memberName = memberName;
        this.reservationDate = reservationDate;
        this.status = status;
    }

    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public int getMemberId() { return memberId; }
    public String getBookTitle() { return bookTitle; }
    public String getMemberName() { return memberName; }
    public LocalDate getReservationDate() { return reservationDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
