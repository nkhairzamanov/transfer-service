package org.superbank.endpoint;

import java.math.BigDecimal;

public class TransferOrder {
    private String fromIban;
    private String toIban;
    private BigDecimal amount;
    private String message;

    public TransferOrder() {
    }

    public TransferOrder(String fromIban, String toIban, BigDecimal amount, String message) {
        this.fromIban = fromIban;
        this.toIban = toIban;
        this.amount = amount;
        this.message = message;
    }

    public String getFromIban() {
        return fromIban;
    }

    public void setFromIban(String fromIban) {
        this.fromIban = fromIban;
    }

    public String getToIban() {
        return toIban;
    }

    public void setToIban(String toIban) {
        this.toIban = toIban;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
