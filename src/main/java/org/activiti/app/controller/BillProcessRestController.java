package org.activiti.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.activiti.app.messages.BillTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
public class BillProcessRestController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private RuntimeService runtimeService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private HistoryService historyService;
    
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/enter-bill", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void enterBill(@RequestBody Map<String, String> data, HttpServletRequest request) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("vendor", data.get("vendor"));
	variables.put("total", data.get("total"));
	log.info("Starting Bill Management Process for vendor: \"" + data.get("vendor") + "\" - total: " + data.get("total"));
        runtimeService.startProcessInstanceByKey("billManagement", variables);

    }
    
    @RequestMapping(value = "/{group}/tasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTasksForGroup(@PathVariable("group") String group, HttpServletRequest request) {
	log.info("Retrieving list of tasks for group: " + group);
	return convertTasks(taskService.createTaskQuery().taskCandidateGroup(group).list());
    }
    
    @RequestMapping(value = "/mytasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTasksForUser(HttpServletRequest request) {
	String user = request.getUserPrincipal().getName();
	log.info("Retrieving list of tasks for user: " + user);
	return convertTasks(taskService.createTaskQuery().taskAssignee(user).list());
    }
    
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/tasks/{id}/claim", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void claimTask(@PathVariable("id") String id, HttpServletRequest request) {
	String user = request.getUserPrincipal().getName();
	log.info("User: \"" + user + "\" claimed task: #" + id);
	taskService.claim(id, user);
    }
    
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/tasks/{id}/complete", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void completeTask(@PathVariable("id") String id, HttpServletRequest request) {
	String user = request.getUserPrincipal().getName();
	log.info("User: \"" + user + "\" completed task: #" + id);
	taskService.complete(id);
    }
    
    @RequestMapping(value = "/myavailabletasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTasksAvailableToUser(HttpServletRequest request) {
	String user = request.getUserPrincipal().getName();
	log.info("Retrieving list of tasks available to user: " + user);
	return convertTasks(taskService.createTaskQuery().taskCandidateUser(user).list());
    }
    
    @RequestMapping(value = "/processedbillscount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getProcessedBills() {
	log.info("Retrieving list of bills that have been processed");
	return "\n\t Number of processed bills: " + Long.toString(historyService.createHistoricProcessInstanceQuery().finished().count()) + "\n\n";
    }    
    
    /*	A simple parser to pretty json for command line output	*/
    private String convertTasks(List<Task> tasks) {	
	ObjectMapper mapper = new ObjectMapper();
	String formatted = null;
	try {
	    formatted = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
		    tasks.stream().map(t -> {
			BillTask bt = new BillTask();
			bt.setId(t.getId());
			bt.setName(t.getName());
			if (t.getAssignee() == null) {
			    bt.setAssingee("Not Assigned");
			} else {
			    bt.setAssingee(t.getAssignee());
			}    
			return bt;
		    }).collect(Collectors.toList())
		);    
	} catch (JsonProcessingException ex) {
	    java.util.logging.Logger.getLogger(BillProcessRestController.class.getName()).log(Level.SEVERE, null, ex);
	}
	return "\n" + formatted + "\n\n";
    } 
}
