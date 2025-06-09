package com.external.domain;

public record Payment(String id, String invoiceId, String customerId, double amount, String paymentDate,
                      String billingAddress, String postalCode) {
}
