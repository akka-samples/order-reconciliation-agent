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

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
@HttpEndpoint("/external/api")
public class CustomerEndpoint {

    private static final Logger log = LoggerFactory.getLogger(CustomerEndpoint.class);
    private final ComponentClient componentClient;

    public CustomerEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Post("/sales")
    public CustomerEntity.CustomerEvent.InvoiceAdded createSalesInvoice(SaleInvoice saleInvoice) {
        return componentClient
                .forEventSourcedEntity(saleInvoice.getCustomerId())
                .method(CustomerEntity::addSalesInvoice)
                .invoke(saleInvoice);
    }

    @Post("/payments")
    public CustomerEntity.CustomerEvent.PaymentAdded createPayment(Payment payment) {
        return componentClient
                .forEventSourcedEntity(payment.customerId())
                .method(CustomerEntity::addPayment)
                .invoke(payment);
    }

    @Get("/reconcile/{customerId}")
    public String reconcile(String customerId) {
        var reconcileResponse = componentClient
                .forEventSourcedEntity(customerId)
                .method(CustomerEntity::reconcile)
                .invoke(new Reconcile(customerId));
        if (!reconcileResponse.isSuccess()) {
            log.info("Reconcile failed, reporting the incident");
            componentClient
                    .forWorkflow("incidents-workflow" + UUID.randomUUID().toString())
                    .method(IncidentWorkflow::process)
                    .invoke(new Incident(UUID.randomUUID().toString(), reconcileResponse.message(), ""));
        }
        return reconcileResponse.message();
    }

    @Get("/incidents")
    public Collection<Incident> getAllIncidents() {
        return componentClient
                .forEventSourcedEntity("incidents")
                .method(IncidentEntity::getIncidents)
                .invoke();
    }

    @Get("/sales/{customerId}/{invoiceId}")
    public SaleInvoice getSaleInvoiceByCustomerId(String customerId, String invoiceId) {
        return componentClient
                .forEventSourcedEntity(customerId)
                .method(CustomerEntity::getSaleInvoice)
                .invoke(invoiceId);
    }


    @Get("/payments/{customerId}/{invoiceId}")
    public Payment getPaymentByCustomerId(String customerId, String invoiceId) {
        return componentClient
                .forEventSourcedEntity(customerId)
                .method(CustomerEntity::getPayment)
                .invoke(invoiceId);
    }

    @Get("/customers/{customerId}")
    public CustomerDetails getCustomerDetails(String customerId) {
        return componentClient
                .forEventSourcedEntity(customerId)
                .method(CustomerEntity::getCustomerDetails)
                .invoke();
    }

}