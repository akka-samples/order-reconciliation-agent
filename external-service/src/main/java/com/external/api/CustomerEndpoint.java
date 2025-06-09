package com.external.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import com.external.application.CustomerEntity;
import com.external.application.IncidentEntity;
import com.external.application.IncidentWorkflow;
import com.external.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/external/api")
public class CustomerEndpoint {

    private static final Logger log = LoggerFactory.getLogger(CustomerEndpoint.class);
    private final ComponentClient componentClient;

    public CustomerEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Post("/sales")
    public CompletionStage<CustomerEntity.CustomerEvent.InvoiceAdded> createSalesInvoice(SaleInvoice saleInvoice) {
        return componentClient
                .forEventSourcedEntity(saleInvoice.getCustomerId())
                .method(CustomerEntity::addSalesInvoice)
                .invokeAsync(saleInvoice);
    }

    @Post("/payments")
    public CompletionStage<CustomerEntity.CustomerEvent.PaymentAdded> createPayment(Payment payment) {
        return componentClient
                .forEventSourcedEntity(payment.customerId())
                .method(CustomerEntity::addPayment)
                .invokeAsync(payment);
    }

    @Get("/reconcile/{customerId}")
    public CompletionStage<String> reconcile(String customerId) {
        return componentClient
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
                                .thenApply(a -> reconcileResponse.message());

                    } else {
                        return CompletableFuture.supplyAsync(reconcileResponse::message);
                    }
                });
    }

    @Get("/incidents")
    public CompletionStage<Collection<Incident>> getAllIncidents() {
        return componentClient
                .forEventSourcedEntity("incidents")
                .method(IncidentEntity::getIncidents)
                .invokeAsync();
    }

    @Get("/sales/{customerId}/{invoiceId}")
    public CompletionStage<SaleInvoice> getSaleInvoiceByCustomerId(String customerId, String invoiceId) {
        return componentClient
                .forEventSourcedEntity(customerId)
                .method(CustomerEntity::getSaleInvoice)
                .invokeAsync(invoiceId);
    }


    @Get("/payments/{customerId}/{invoiceId}")
    public CompletionStage<Payment> getPaymentByCustomerId(String customerId, String invoiceId) {
        return componentClient
                .forEventSourcedEntity(customerId)
                .method(CustomerEntity::getPayment)
                .invokeAsync(invoiceId);
    }

    @Get("/customers/{customerId}")
    public CompletionStage<CustomerDetails> getCustomerDetails(String customerId) {
        return componentClient
                .forEventSourcedEntity(customerId)
                .method(CustomerEntity::getCustomerDetails)
                .invokeAsync();
    }

}
