package com.freshworks.FreshService.worker;


import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.sdk.workflow.task.WorkerTask;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import com.google.common.collect.ImmutableMap;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.freshworks.FreshService.connector.service.TicketConnectorService;
import com.freshworks.FreshService.connector.request.TicketConnectorRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import com.freshworks.FreshService.connector.service.ConnectorHagridConfiguration;
import lombok.extern.slf4j.Slf4j;
import com.freshworks.FreshService.connector.util.TicketConnectorUtil;
import com.freshworks.FreshService.worker.util.WorkerUtil;



@Slf4j
@Component
public class TicketWorker {

   private final TaskClient httpTaskClient;
   private final TicketConnectorService ticketConnectorService;

   ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public TicketWorker(TaskClient httpTaskClient , TicketConnectorService ticketConnectorService) {
        this.httpTaskClient = httpTaskClient;
        this.ticketConnectorService = ticketConnectorService;
    }


    @WorkerTask(value = "create_fs_ticket_connector")
    public TaskResult work(Task task) {

            log.info("worker task: create_fs_ticket_connector invoked");

            TicketConnectorRequest ticketConnectorRequest = WorkerUtil.getConnectorRequest(task);
            ConnectorHagridConfiguration connectorHagridConfiguration = new ConnectorHagridConfiguration();
            Map<String, String> inputData = TicketConnectorUtil.convertConnectorRequestDataToHagridMap(ticketConnectorRequest);

            ImmutableMap<String , String> baggageMap = ticketConnectorService.filterAndCreateBaggageMap(inputData);

        Object o = TicketConnectorUtil.callFSService(baggageMap);
//            MetricsClient metricsClient = MetricsClientImpl.create(baggageMap);

//            ticketConnectorService.startSync(baggageMap, ticketConnectorRequest , connectorHagridConfiguration);
//            metricsClient.setStartTime(String.valueOf(System.currentTimeMillis()));


//             while(!jiraissueConnectorService.isSyncComplete(connectorHagridConfiguration)) {
//
//              }

 //            Map<String,Object> output = ticketConnectorService.consume(ticketConnectorRequest , connectorHagridConfiguration);

        Map<String,Object> output = new HashMap();
        output.put("data", o);

        task.setStatus(Task.Status.COMPLETED);
//        metricsClient.setEndTime(String.valueOf(System.currentTimeMillis()));
//        metricsClient.flush();
        task.setOutputData(output);
        System.out.println("invoked");
//        ticketConnectorService.clearData(connectorHagridConfiguration);
        return new TaskResult(task);
    }




}


