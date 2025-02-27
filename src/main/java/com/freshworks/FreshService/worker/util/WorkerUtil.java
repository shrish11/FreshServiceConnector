package com.freshworks.FreshService.worker.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.freshworks.FreshService.common.util.JsonUtil;
import com.freshworks.FreshService.connector.request.TicketConnectorRequest;
import com.netflix.conductor.common.metadata.tasks.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.freshworks.platform.IPCommonUtil;

@Slf4j
public class WorkerUtil {

    public static TicketConnectorRequest getConnectorRequest(Task task) {

               String requestId = IPCommonUtil.getNameSpace(task);

               Map<String, Object> workflowInput = new HashMap<>();
               Map<String, Object> inputData = task.getInputData();
               try {
                      if(inputData != null && inputData.get("workflow") != null) {
                           workflowInput = (Map<String,Object>)inputData.get("workflow");
                     }

                    } catch (Exception e) {

                         log.error(e.getMessage());
                          throw new RuntimeException(e);
                    }

               Map<String, Object> inputRequiredFrom = new HashMap<>();
               try {
                       if (inputData != null && inputData.containsKey("input_required_from")) {
                               inputRequiredFrom = (Map<String, Object>) inputData.get("input_required_from");
                               System.out.println("Input Required From:");
                           }
                    } catch (Exception e) {
                          log.error(e.getMessage());
                          throw new RuntimeException(e);
                    }
               try {
               return TicketConnectorRequest.
                             builder().requestId(requestId)
                             .workflowInput(workflowInput)
                             .inputRequiredFrom(inputRequiredFrom)
                             .accountId(Optional.ofNullable(task.getInputData().get("accountId"))
                                     .map(Object::toString)
                                     .map(Long::parseLong)
                                     .orElse(null))
                             .data(JsonUtil.parseAsJsonNode(task.getInputData().get("data")))
                             .task(task)
                             .build();
                 } catch (JsonProcessingException e) {
                         log.error(e.getMessage());
                          throw new RuntimeException(e);
                    }

           }



}
