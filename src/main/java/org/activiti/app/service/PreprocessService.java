package org.activiti.app.service;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PreprocessService {
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public void preProcessBill(DelegateExecution execution) {
	
	log.info("Preprocessing bill service executed. Process id: " + execution.getProcessInstanceId() + " with data:");
	execution.getVariables().entrySet().stream()
		.forEach(e -> log.info(e.getKey() + " - " + e.getValue()));
    }    
    
}
