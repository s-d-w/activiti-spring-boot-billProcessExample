#
# Activiti Spring-boot bill processs example

A simple proof of concept bpmn process for processing a bill. I.e, entering the bill into the system, validating, approving, and paying. There's no functionality in each step other than claiming it and then completing it.


### Installation

pre. Install Java 1.8 and install Maven using any means. I.e,

    brew install maven

1. mvn clean install

2. mvn spring-boot:run


### Note

Http basic auth is used (for now). To enter a bill into the system (start a new bill process), you can be any user that's created by the initializing bean. There are no fine grained permissions right now.

There are two service tasks which demonstrate automatic processing such as payment processing.

BPMN diagram (from Activiti Eclipse plugin)

![BPMN Diagram](billProcDiagram.png?raw=true "BPMN Diagram") 


### Usage

The server listens on

    localhost:8080

There are 4 user groups, each with one user: vendors, validators, approvers, payers. The users are (username:pass) vendor:pw, validator:pw, approver:pw, payer:pw, and a generic account not related to activiti user groups of admin:admin (for http basic auth).

The first step is to enter a few bills into the system. (repeat below any number of times)
    
    curl -u admin:admin -H "Content-Type: application/json" -d '{"vendor":"A Corp", "total": "5000.00"}' http://localhost:8080/enter-bill

Optionally you can view the tasks that are available to claim for each group

    curl -u admin:admin -H "Content-Type: application/json" http://localhost:8080/validators/tasks
    curl -u admin:admin -H "Content-Type: application/json" http://localhost:8080/payers/tasks
    etc ..

Get a task id by listing tasks either by using the above or by listing tasks available to a user who belongs to a specific group which has group permissions for that task

    curl -u validator:pw -H "Content-Type: application/json" http://localhost:8080/myavailabletasks
    curl -u approver:pw -H "Content-Type: application/json" http://localhost:8080/myavailabletasks
    etc ..

Claim a task (as a validator) where {id} is a task id listed from above (note: REMOVE THE BRACES before actually running the curl command)

    curl -u validator:pw -H "Content-Type: application/json" -X PUT http://localhost:8080/tasks/{id}/claim

List tasks that you've claimed

    curl -u validator:pw -H "Content-Type: application/json" http://localhost:8080/mytasks

Complete a task that you've claimed (where {id} is a task id)

    curl -u validator:pw -H "Content-Type: application/json" -X PUT http://localhost:8080/tasks/{id}/complete

There will now be a task available for the approver which can be listed, claimed, and completed similarly to the above routine but as the user 'approver'. Repeat for each successive user to process a bill transaction.

To verify that a bill has completed processing

    curl -u admin:admin -H "Content-Type: application/json" http://localhost:8080/processedbillscount
