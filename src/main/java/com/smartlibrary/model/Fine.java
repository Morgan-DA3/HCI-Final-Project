package com.smartlibrary.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Fine {
    private final int id;
    private int borrowingId;
    private int memberId;
    private BigDecimal amount;
    private String reason;
    private String status;
    private LocalDate createdDate;

    public Fine(int id, int borrowingId, int memberId, BigDecimal amount, String reason, String status, LocalDate createdDate) {
        this.id = id;
        this.borrowingId = borrowingId;
        this.memberId = memberId;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.createdDate = createdDate;
    }

    public int getId() { return id; }
    public int getBorrowingId() { return borrowingId; }
    public int getMemberId() { return memberId; }
    public BigDecimal getAmount() { return amount; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public LocalDate getCreatedDate() { return createdDate; }
}
