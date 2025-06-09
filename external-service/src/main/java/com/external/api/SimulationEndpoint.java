package com.external.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import com.external.api.model.Simulate;
import com.external.application.CustomerEntity;
import com.external.application.IncidentWorkflow;
import com.external.domain.Incident;
import com.external.domain.Payment;
import com.external.domain.Reconcile;
import com.external.domain.SaleInvoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/external/api")
public class SimulationEndpoint {

    private static final Logger log = LoggerFactory.getLogger(SimulationEndpoint.class);
    private String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final ComponentClient componentClient;

    public SimulationEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Post("/simulation")
    public CompletionStage<String> getAllIncidents(Simulate simulate) {
        return simulate(simulate);
    }


    private CompletionStage<String> simulate(Simulate simulate) {
        return CompletableFuture.supplyAsync(() -> {
            generateAddressIssues(simulate);
            generatePaymentMismatchIssues(simulate);
            generateReconciliationBeforePaymentIssues(simulate);
            return "created";
        });
    }

    private void generateReconciliationBeforePaymentIssues(Simulate simulate) {
        if (simulate.nrOfReconciliationBeforePaymentIssues() <= 0) return;
        List<String> invoices = generateIds("invoice-", simulate.nrOfReconciliationBeforePaymentIssues());
        List<String> customers = generateIds("customer-", simulate.nrOfReconciliationBeforePaymentIssues());
        List<String> paymentIds = generateIds("pay-", simulate.nrOfReconciliationBeforePaymentIssues());
        for (int i = 0; i < simulate.nrOfReconciliationBeforePaymentIssues(); i++) {
            createAndReconcile(invoices, customers, paymentIds, i, 1500, 1500, true, "123 street", "50505");
        }
    }

    private void generatePaymentMismatchIssues(Simulate simulate) {
        if (simulate.nrOfPaymentMismatchIssues() <= 0) return;
        List<String> invoices = generateIds("invoice-", simulate.nrOfPaymentMismatchIssues());
        List<String> customers = generateIds("customer-", simulate.nrOfPaymentMismatchIssues());
        List<String> paymentIds = generateIds("pay-", simulate.nrOfPaymentMismatchIssues());
        for (int i = 0; i < simulate.nrOfPaymentMismatchIssues(); i++) {
            createAndReconcile(invoices, customers, paymentIds, i, 1500.0, 1300.0, false, "123 street", "50505");
        }
    }

    private void generateAddressIssues(Simulate simulate) {
        if (simulate.nrOfAddressIssues() <= 0) return;
        List<String> invoices = generateIds("invoice-", simulate.nrOfAddressIssues());
        List<String> customers = generateIds("customer-", simulate.nrOfAddressIssues());
        List<String> paymentIds = generateIds("pay-", simulate.nrOfAddressIssues());
        for (int i = 0; i < simulate.nrOfAddressIssues(); i++) {
            createAndReconcile(invoices, customers, paymentIds, i, 1500.0, 1500.0, false, null, null);
        }
    }

    private void createAndReconcile(List<String> invoices, List<String> customers, List<String> paymentIds, int i, double saleAmount, double paymentAmount, boolean reconcileBeforePayment, String billingAddress, String postalCode) {
        SaleInvoice saleInvoice = new SaleInvoice(invoices.get(i), customers.get(i), saleAmount, "pending", getDate());
        Payment payment = new Payment(paymentIds.get(i), invoices.get(i), customers.get(i), paymentAmount, getDate(), billingAddress, postalCode);
        addSalesData(customers.get(i), saleInvoice);
        if (reconcileBeforePayment) {
            reconcile(customers.get(i));
        }
        addPayment(customers.get(i), payment);
        if (!reconcileBeforePayment) {
            reconcile(customers.get(i));
        }

    }

    private void addSalesData(String customerId, SaleInvoice saleInvoice) {
        componentClient
                .forEventSourcedEntity(customerId)
                .method(CustomerEntity::addSalesInvoice)
                .invokeAsync(saleInvoice)
                .toCompletableFuture().join();
    }

    private void reconcile(String customerId) {
        componentClient
                .forEventSourcedEntity(customerId)
                .method(CustomerEntity::reconcile)
                .invokeAsync(new Reconcile(customerId))
                .thenCompose(reconcileResponse -> {
                    if (!reconcileResponse.isSuccess()) {
                        log.info("Reconcile failed, reporting the incident");
                        return componentClient
                                .forWorkflow("incidents-workflow" + UUID.randomUUID().toString())
                                .method(IncidentWorkflow::process)
                                .invokeAsync(new Incident(UUID.randomUUID().toString(), reconcileResponse.message(), ""))
                                .thenApply(a -> reconcileResponse);
                    } else {
                        return CompletableFuture.supplyAsync(() -> reconcileResponse);
                    }
                }).toCompletableFuture().join();
    }

    private void addPayment(String customerId, Payment payment) {
        componentClient
                .forEventSourcedEntity(payment.customerId())
                .method(CustomerEntity::addPayment)
                .invokeAsync(payment).toCompletableFuture().join();
    }

    private String getDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return currentDate.format(formatter);
    }

    public List<String> generateIds(String prefix, int count) {
        Random random = new Random();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            StringBuilder randomId = new StringBuilder(prefix);
            for (int j = 0; j < 8; j++) {
                randomId.append(characters.charAt(random.nextInt(characters.length())));
            }
            ids.add(randomId.toString());
        }

        return ids;
    }
}
