package com.akka.application.model;

public record Payment(
        String id,
        String invoiceId,
        double amount,
        String customerId,
        String paymentDate,
        String billingAddress,
        String postalCode
) {
}