package com.external.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.TypeName;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.external.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@ComponentId("customer")
public class CustomerEntity extends EventSourcedEntity<CustomerEntity.CustomerState, CustomerEntity.CustomerEvent> {

    private static final Logger log = LoggerFactory.getLogger(CustomerEntity.class);
    private final String customerId;

    public CustomerEntity(EventSourcedEntityContext context) {
        this.customerId = context.entityId();
    }

    public Effect<CustomerEvent.InvoiceAdded> addSalesInvoice(SaleInvoice s) {
        CustomerEvent.InvoiceAdded invoiceAdded = new CustomerEvent.InvoiceAdded(s.getInvoiceId(), s.getCustomerId(), s.getAmount(), s.getStatus(), s.getDate());
        return effects()
                .persist(invoiceAdded)
                .thenReply(newState -> invoiceAdded);
    }

    public Effect<CustomerEvent.PaymentAdded> addPayment(Payment p) {
        CustomerEvent.PaymentAdded paymentAdded =
                new CustomerEvent.PaymentAdded(p.id(), p.invoiceId(), p.customerId(), p.amount(), p.paymentDate(), p.billingAddress(), p.postalCode());
        Map<String, Payment> invoiceIdToPayments = currentState().invoiceIdToPayments();
        Map<String, SaleInvoice> invoiceIdToSaleInvoice = currentState().invoiceIdToSaleInvoice();
        if (invoiceIdToPayments.containsKey(paymentAdded.invoiceId())) {
            log.info("updating existing payment for invoice id: {}", paymentAdded.invoiceId());
            Payment existing = invoiceIdToPayments.get(paymentAdded.invoiceId());
            double invoiceAmount = invoiceIdToSaleInvoice.get(paymentAdded.invoiceId()).getAmount();
            double oldAmount = existing.amount();
            double newAmount = paymentAdded.amount();
            if (oldAmount + newAmount != invoiceAmount) {
                log.warn("Payment amount mismatch. Autocorrecting.");
            }
            paymentAdded =
                    new CustomerEvent.PaymentAdded(p.id(), p.invoiceId(), p.customerId(), invoiceAmount, p.paymentDate(), p.billingAddress(), p.postalCode());
        }
        CustomerEvent.PaymentAdded finalPaymentAdded = paymentAdded;
        return effects()
                .persist(paymentAdded)
                .thenReply(newState -> finalPaymentAdded);
    }


    public Effect<ReconcileResponse> reconcile(Reconcile reconcile) {
        log.info("Reconciliation received: {}", reconcile);
        List<SaleInvoice> pendingInvoices = currentState().invoiceIdToSaleInvoice().values()
                .stream()
                .filter(s -> s.getStatus().equalsIgnoreCase("pending"))
                .toList();
        log.info("Total pending invoices {}", pendingInvoices.size());
        String message = """
                Reconciliation Failure
                Sales Invoice: id: %s, customerId: %s, amount: %f, status: %s, date: %s
                Payment Details: %s
                """;
        StringBuilder result = new StringBuilder();
        AtomicBoolean isSuccess = new AtomicBoolean(true);
        Map<String, Payment> invoiceIdToPayments = currentState().invoiceIdToPayments();
        pendingInvoices.forEach(saleInvoice -> {
            if (invoiceIdToPayments.containsKey(saleInvoice.getInvoiceId())) {
                if (saleInvoice.getStatus().equalsIgnoreCase("pending")
                        && paymentMatchesWithInvoice(saleInvoice, invoiceIdToPayments)
                        && isBillingAddressOk(saleInvoice, invoiceIdToPayments)) {
                    log.info("Reconcile is success {}", saleInvoice);
                    result.append("Reconciliation for invoice id: " + saleInvoice.getInvoiceId() + "processed successfully");
                    isSuccess.set(true);
                    saleInvoice.setStatus("completed");
                } else if (!isBillingAddressOk(saleInvoice, invoiceIdToPayments)) {
                    Payment payment = invoiceIdToPayments.get(saleInvoice.getInvoiceId());
                    String paymentDetails = String.format("""
                            id: %s, invoiceId: %s, customerId: %s, amount %f,  paymentDate: %s, billingAddress: %s, postalCode: %s
                            """, payment.id(), payment.invoiceId(), payment.customerId(), payment.amount(), payment.paymentDate(), payment.billingAddress(), payment.postalCode());
                    String failureReason = String.format(
                            message,
                            saleInvoice.getInvoiceId(),
                            saleInvoice.getCustomerId(),
                            saleInvoice.getAmount(),
                            saleInvoice.getStatus(),
                            saleInvoice.getDate(),
                            paymentDetails
                    );
                    log.warn("Reconcile is failed {}", failureReason);
                    result.append("Reconcile is failed due to missing billing address. Please retry with the billing address" + failureReason);
                    isSuccess.set(false);
                    //reportIncidentIfNotReportedAlready(failureReason);
                } else {
                    Payment payment = invoiceIdToPayments.get(saleInvoice.getInvoiceId());
                    String paymentDetails = String.format("""
                            id: %s, invoiceId: %s, customerId: %s, amount %f,  paymentDate: %s, billingAddress: %s, postalCode: %s
                            """, payment.id(), payment.invoiceId(), payment.customerId(), payment.amount(), payment.paymentDate(), payment.billingAddress(), payment.postalCode());
                    String failureReason = String.format(
                            message,
                            saleInvoice.getInvoiceId(),
                            saleInvoice.getCustomerId(),
                            saleInvoice.getAmount(),
                            saleInvoice.getStatus(),
                            saleInvoice.getDate(),
                            paymentDetails
                    );
                    log.warn("Reconcile is failed {}", failureReason);
                    result.append("Reconcile is failed " + failureReason);
                    isSuccess.set(false);
                    // reportIncidentIfNotReportedAlready(failureReason);
                }
            } else {
                String failureReason = String.format(
                        message,
                        saleInvoice.getInvoiceId(),
                        saleInvoice.getCustomerId(),
                        saleInvoice.getAmount(),
                        saleInvoice.getStatus(),
                        saleInvoice.getDate(),
                        "No Payment Details found. If payment not made, please complete the payment ASAP"
                );
                log.warn("Reconcile is failed {}", failureReason);
                result.append("Reconcile is failed " + failureReason);
                isSuccess.set(false);
                //reportIncidentIfNotReportedAlready(failureReason);
            }
        });
        var reconcileResponse = new ReconcileResponse(customerId, result.toString().isBlank() ? "No records" : result.toString(), isSuccess.get());
        log.info("Reconcile response {}", reconcileResponse);
        return effects().reply(reconcileResponse);
    }

    private boolean paymentMatchesWithInvoice(SaleInvoice saleInvoice, Map<String, Payment> invoiceIdToPayments) {
        Payment payment = invoiceIdToPayments.get(saleInvoice.getInvoiceId());
        return saleInvoice.getAmount() == payment.amount();
    }

    private boolean isBillingAddressOk(SaleInvoice saleInvoice, Map<String, Payment> invoiceIdToPayments) {
        Payment payment = invoiceIdToPayments.get(saleInvoice.getInvoiceId());
        boolean billingAddressPresent = payment.billingAddress() != null && !payment.billingAddress().isBlank();
        boolean postalCodePresent = payment.postalCode() != null && !payment.postalCode().isBlank();
        return billingAddressPresent && postalCodePresent;
    }

    @Override
    public CustomerState emptyState() {
        return new CustomerState(customerId, new HashMap<>(), new HashMap<>());
    }

    @Override
    public CustomerState applyEvent(CustomerEvent event) {
        return switch (event) {
            case CustomerEvent.InvoiceAdded evt -> currentState()
                    .updateInvoice(evt.invoiceId(), new SaleInvoice(
                            evt.invoiceId(),
                            evt.customerId(),
                            evt.amount(),
                            evt.status(),
                            evt.date()
                    ));
            case CustomerEvent.PaymentAdded evt -> currentState().updatePayment(evt.invoiceId(), new Payment(
                    evt.id(),
                    evt.invoiceId(),
                    evt.customerId(),
                    evt.amount(),
                    evt.paymentDate(),
                    evt.billingAddress(),
                    evt.postalCode()
            ));
            default -> throw new IllegalStateException("Unexpected value: " + event);
        };
    }

    public ReadOnlyEffect<SaleInvoice> getSaleInvoice(String invoiceId) {
        return effects().reply(currentState().invoiceIdToSaleInvoice.get(invoiceId));
    }

    public ReadOnlyEffect<Payment> getPayment(String invoiceId) {
        return effects().reply(currentState().invoiceIdToPayments.get(invoiceId));
    }

    public ReadOnlyEffect<CustomerDetails> getCustomerDetails() {
        return effects().reply(new CustomerDetails("123 Street Name", "12345", customerId));
    }

    public sealed interface CustomerEvent {
        @TypeName("invoice-added")
        record InvoiceAdded(String invoiceId, String customerId, double amount, String status,
                            String date) implements CustomerEvent {
        }

        @TypeName("payment-added")
        record PaymentAdded(String id, String invoiceId, String customerId, double amount, String paymentDate,
                            String billingAddress, String postalCode) implements CustomerEvent {
        }

        @TypeName("reconciled")
        record Reconciled() implements CustomerEvent {
        }
    }

    public record CustomerState(String customerId, Map<String, SaleInvoice> invoiceIdToSaleInvoice,
                                Map<String, Payment> invoiceIdToPayments) {

        public CustomerState updatePayment(String invoiceId, Payment payment) {
            invoiceIdToPayments.put(invoiceId, payment);
            return new CustomerState(invoiceId, invoiceIdToSaleInvoice, invoiceIdToPayments);
        }

        public CustomerState updateInvoice(String invoiceId, SaleInvoice saleInvoice) {
            invoiceIdToSaleInvoice.put(invoiceId, saleInvoice);
            log.info("Updating invoice {}, current invoices {}", invoiceId, invoiceIdToSaleInvoice.size());
            return new CustomerState(invoiceId, invoiceIdToSaleInvoice, invoiceIdToPayments);
        }
    }


}
