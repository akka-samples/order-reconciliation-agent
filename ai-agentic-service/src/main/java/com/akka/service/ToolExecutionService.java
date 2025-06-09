package com.akka.service;

import com.akka.application.model.ChainOfThought;
import com.akka.application.model.CustomerDetails;
import com.akka.application.model.Payment;
import com.akka.application.model.SalesInvoice;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutionService.class);
    private final String productionSystemUrl;
    private final SimpleHttpClient httpClient;
    private final AsyncHttpClient client;
    private final Config config;

    public ToolExecutionService(Config config) {
        this.config = config;
        productionSystemUrl = config.getString("retailer.productionSystemUrl");
        client = new AsyncHttpClient();
        httpClient = new SimpleHttpClient();
    }

    public String execute(ChainOfThought chainOfThought, String toolOutput) {
        if (chainOfThought.PAUSE() && chainOfThought.Tool() != null) {
            //log.info("Executing the LLM suggested tool {} with input {}", chainOfThought.Tool(), chainOfThought.input());
            String tool = chainOfThought.Tool();
            if (tool.equalsIgnoreCase("get_customer_details")) {
                log.info("Executing Tool: get_customer_details with input {}", chainOfThought.input());
                CustomerDetails customerDetails = httpClient.get(productionSystemUrl + "/external/api/customers/" + chainOfThought.input(), CustomerDetails.class);
                toolOutput = String.format(toolOutput, customerDetails);
            } else if (tool.equalsIgnoreCase("get_sales_details")) {
                log.info("Executing Tool: get_sales_details with input {}", chainOfThought.input());
                String input = String.valueOf(chainOfThought.input());
                String customerInvoice = input.replace(",", "/").replaceAll(" ", "");
                SalesInvoice salesInvoice = httpClient.get(productionSystemUrl + "/external/api/sales/" + customerInvoice, SalesInvoice.class);
                toolOutput = String.format(toolOutput, salesInvoice.toString());
            } else if (tool.equalsIgnoreCase("get_payment_details")) {
                log.info("Executing Tool: get_payment_details with input {}", chainOfThought.input());
                String input = chainOfThought.input();
                String customerInvoice = input.replace(",", "/").replaceAll(" ", "");
                Payment payment = httpClient.get(productionSystemUrl + "/external/api/payments/" + customerInvoice, Payment.class);
                toolOutput = String.format(toolOutput, payment.toString());
            } else if (tool.equalsIgnoreCase("update_payment_request")) {
                log.info("Executing Tool: update_payment_request with input {}", chainOfThought.input());
                String input = chainOfThought.input();
                String[] paymentValues = input.split(",");
                if (paymentValues.length != 7) {
                    toolOutput = String.format(toolOutput, "The input given is not in the expected format. LLM you failed me!!!");
                } else {
                    Payment pay = new Payment(paymentValues[0], paymentValues[1], Double.valueOf(paymentValues[2]), paymentValues[3], paymentValues[4], paymentValues[5], paymentValues[6]);
                    Payment payment = httpClient.post(productionSystemUrl + "/external/api/payments", pay, Payment.class);
                    toolOutput = String.format(toolOutput, "201 Created. Updated Payment details " + payment.toString());
                }
            } else if (tool.equalsIgnoreCase("retry_reconciliation_request")) {
                log.info("Executing Tool: retry_reconciliation_request with input {}", chainOfThought.input());
                String input = String.valueOf(chainOfThought.input());
                String reconcileResp = httpClient.get(productionSystemUrl + "/external/api/reconcile/" + input, String.class);
                toolOutput = String.format(toolOutput, reconcileResp);
            } else {
                log.error("Selected Unknown Tool: {}", chainOfThought.Tool());
                toolOutput = String.format(toolOutput, "invalid tool selected. There is no such tool");
            }
        }

        return toolOutput;
    }
}
