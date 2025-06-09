package com.akka.application.model;

public record SalesInvoice(
        String invoiceId,
        String customerId,
        double amount,
        String status,
        String date
        ) {
}
