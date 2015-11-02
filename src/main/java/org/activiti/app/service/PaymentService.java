package org.activiti.app.service;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PaymentService {
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public void processPayment(DelegateExecution execution) {
        log.info("Processing payment for Vendor: \"" + execution.getVariable("vendor") + "\" with total: " + execution.getVariable("total"));	
    }

}
