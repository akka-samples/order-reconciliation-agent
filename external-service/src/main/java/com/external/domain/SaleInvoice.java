package com.external.domain;

import java.util.Objects;

public class SaleInvoice {
    private  String invoiceId;
    private  String customerId;
    private  double amount;
    private  String status;
    private  String date;

    public SaleInvoice() {

    }
    public SaleInvoice(String invoiceId, String customerId, double amount, String status, String date) {
        this.invoiceId = invoiceId;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
        this.date = date;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SaleInvoice) obj;
        return Objects.equals(this.invoiceId, that.invoiceId) &&
                Objects.equals(this.customerId, that.customerId) &&
                Double.doubleToLongBits(this.amount) == Double.doubleToLongBits(that.amount) &&
                Objects.equals(this.status, that.status) &&
                Objects.equals(this.date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId, customerId, amount, status, date);
    }

    @Override
    public String toString() {
        return "SaleInvoice[" +
                "id=" + invoiceId + ", " +
                "customerId=" + customerId + ", " +
                "amount=" + amount + ", " +
                "status=" + status + ", " +
                "date=" + date + ']';
    }

}
