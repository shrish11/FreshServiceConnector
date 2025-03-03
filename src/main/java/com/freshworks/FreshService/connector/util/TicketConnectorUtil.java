package com.freshworks.FreshService.connector.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshworks.FreshService.common.util.CommonUtil;
import com.freshworks.FreshService.common.util.JsonUtil;
import com.freshworks.FreshService.connector.dtos.*;
import com.freshworks.FreshService.connector.request.TicketConnectorRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class TicketConnectorUtil {

     public static Map<String,String> convertConnectorRequestDataToHagridMap(TicketConnectorRequest connectorRequest) {

            Map<String, String> workflowMap = new HashMap<>();
            Map<String,String> inputFromOtherConnector = new HashMap<>();
            try {
                //workflowMap = CommonUtil.convertMap(connectorRequest.getWorkflowInput());
                Map<String, Object> workflowInput = connectorRequest.getWorkflowInput();
                 String workflowInputStr = JsonUtil.toJsonString(workflowInput);
                 workflowMap.put("workflowInput", workflowInputStr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
               // inputFromOtherConnector = CommonUtil.convertMap(connectorRequest.getInputRequiredFrom());
                String inputFromOtherConnectorStr = JsonUtil.toJsonString(connectorRequest.getInputRequiredFrom());
                inputFromOtherConnector.put("inputRequiredFrom", inputFromOtherConnectorStr);
                System.out.println("stringStringMap: "+inputFromOtherConnector);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            Map<String,String> taskData = new HashMap<>();

            try{
                 String taskJson = JsonUtil.toJsonString(connectorRequest.getTask());

                 taskData.put("task" , taskJson);
            }catch (Exception e) {
                 throw new RuntimeException(e);
            }

             Map<String, String> mergedMaps = CommonUtil.mergeMaps(workflowMap, inputFromOtherConnector);
             return CommonUtil.mergeMaps(taskData , mergedMaps);
        }

         public static Map<String, Object> getWorkFlowInputData(ImmutableMap<String, String> baggageMap) throws IOException {

               String workflowInput = baggageMap.get("workflowInput");
               return JsonUtil.parseAsObject(workflowInput, new TypeReference<>() {});

           }

         public static Map<String, Object> getInputFromOtherConnector(ImmutableMap<String, String> baggageMap) throws IOException {

                String inputRequiredFrom = baggageMap.get("inputRequiredFrom");
                return JsonUtil.parseAsObject(inputRequiredFrom, new TypeReference<>() {});

          }


    public static Object  callFSService(ImmutableMap<String , String> baggageMap){

        Map<String, Object> workFlowInputData = null;
        try {
            workFlowInputData = getWorkFlowInputData(baggageMap);
            ConnectorData connectorData = getConnectorData(workFlowInputData);
            Map<String, Object> inputFromOtherConnector = getInputFromOtherConnector(baggageMap);
            String op = connectorData.getOp();
            switch (op){
                case "create_fs_ticket": return createFSTicket(connectorData, inputFromOtherConnector);
//                case "create_fs_user": return createFSUser(connectorData);


                default: throw new RuntimeException("Unsupported Operation");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    private static Object createFSTicket(ConnectorData connectorData, Map<String, Object> inputFromOtherConnector) throws IOException {

        Map<String, Object> operationData = connectorData.getOperationData();
        JsonNode jsonNode = JsonUtil.parseAsJsonNode(operationData.get("create_fs_ticket"));
        FSConnectionDetails fsConnectionDetails = FSConnectionDetails.builder()
                .accountDomain(jsonNode.get("fs_account").asText())
                .user(jsonNode.get("fs_user").asText())
                .password(jsonNode.get("fs_pwd").asText())
                .build();
        JsonNode ticketsNode = jsonNode.get("tickets");

        List<String> ticketIds = new ArrayList<>();
        if(ticketsNode.isArray()){
            for(JsonNode ticketNode : ticketsNode){
                ticketIds.add(createSingleFSTicket(ticketNode, fsConnectionDetails, inputFromOtherConnector));
            }
        }

        return ticketIds;

    }

    private static String createSingleFSTicket(JsonNode ticketNode, FSConnectionDetails fsConnectionDetails, Map<String, Object> inputFromOtherConnector) throws IOException {

        String createTicketUrl = "https://{domain}.freshservice.com/api/v2/tickets";
        String uriString = UriComponentsBuilder.fromUriString(createTicketUrl)
                .buildAndExpand(fsConnectionDetails.getAccountDomain())
                .toUriString();

        String auth = getAuth(fsConnectionDetails.getPassword(), fsConnectionDetails.getUser());
        List<FSDepartment> departments = fetchFSDepartmentList(fsConnectionDetails);
        List<FSGroup> fsGroups = fetchFSGroupList((fsConnectionDetails));
        FreshserviceTicket freshserviceTicket = createFSTicketModel(ticketNode, inputFromOtherConnector, departments, fsGroups);

        RestClient restClient = RestClient.create();


        ResponseEntity<String> response = restClient.post()
                .uri(uriString)
                .header(HttpHeaders.AUTHORIZATION, "Basic "+auth)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.ACCEPT, "application/json")
                .body(freshserviceTicket)
                .retrieve()
                .toEntity(String.class);

        HttpStatusCode statusCode = response.getStatusCode();
        String body = response.getBody();
        JsonNode agentBody = JsonUtil.parseAsJsonNode(body);
        JsonNode jsonNodeAgent = agentBody.get("ticket");
        String ticketId = jsonNodeAgent.get("id").asText();
        System.out.println("Ticket created: "+ticketId);
        return ticketId;

//        return statusCode.is2xxSuccessful() ? "Ticket Created Successfully" : "Failed to create ticket";
    }

    private static List<FSDepartment> fetchFSDepartmentList(FSConnectionDetails fsConnectionDetails) throws IOException {

        String createTicketUrl = "https://{domain}.freshservice.com/api/v2/departments";
        String uriString = UriComponentsBuilder.fromUriString(createTicketUrl)
                .buildAndExpand(fsConnectionDetails.getAccountDomain())
                .toUriString();
        String auth = getAuth(fsConnectionDetails.getPassword(), fsConnectionDetails.getUser());

        RestClient restClient = RestClient.create();

        ResponseEntity<String> entity = restClient.get()
                .uri(uriString)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + auth)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .toEntity(String.class);

        String body = entity.getBody();
        JsonNode jsonNode = JsonUtil.parseAsJsonNode(body);
        JsonNode departmentNode = jsonNode.get("departments");
        List<FSDepartment> departments = new ArrayList<>();
//        if(departmentNode.isArray()){
            for(JsonNode node : departmentNode){
                FSDepartment fsDepartment = JsonUtil.parseAsObject(node, FSDepartment.class);
                departments.add(fsDepartment);
            }
//        }
        return departments;
    }

    private static List<FSGroup> fetchFSGroupList(FSConnectionDetails fsConnectionDetails) throws IOException {

        String createTicketUrl = "https://{domain}.freshservice.com/api/v2/groups";
        String uriString = UriComponentsBuilder.fromUriString(createTicketUrl)
                .buildAndExpand(fsConnectionDetails.getAccountDomain())
                .toUriString();
        String auth = getAuth(fsConnectionDetails.getPassword(), fsConnectionDetails.getUser());

        RestClient restClient = RestClient.create();

        ResponseEntity<String> entity = restClient.get()
                .uri(uriString)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + auth)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .toEntity(String.class);

        String body = entity.getBody();
        JsonNode jsonNode = JsonUtil.parseAsJsonNode(body);
        JsonNode groupNode = jsonNode.get("groups");
        List<FSGroup> groups = new ArrayList<>();
//        if(departmentNode.isArray()){
        for(JsonNode node : groupNode){
            FSGroup fsGroup = JsonUtil.parseAsObject(node, FSGroup.class);
            groups.add(fsGroup);
        }
//        }
        return groups;
    }


    private static FreshserviceTicket createFSTicketModel(JsonNode ticketNode, Map<String, Object> inputFromOtherConnector,
                                                          List<FSDepartment> departments,
                                                          List<FSGroup> groups) throws IOException {

        FreshserviceTicket freshserviceTicket = JsonUtil.parseAsObject(ticketNode, FreshserviceTicket.class);
        Long groupId = getGroupId(ticketNode, groups);
        Long departmentId = departmentId(ticketNode, departments);
        freshserviceTicket.setDepartmentId(departmentId);
        freshserviceTicket.setGroupId(groupId);

        Object createFsUserConnector = inputFromOtherConnector.get("create_fs_user_connector");
        Map<String, Object> output = JsonUtil.parseAsObject(createFsUserConnector, new TypeReference<>() {
        });

        List data = (List) output.get("data");
        Long requesterId = Long.parseLong((String)data.get(0));


        freshserviceTicket.setRequesterId(requesterId);

        return freshserviceTicket;
    }

    private static String getAuth(String password, String user) {

        String auth = password+":"+user;
//        String auth = "dbe5b4ebd227cc1006ef9135b79ee8e7b8795ad7:testSR"; // Use real credentials
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }

    private static Long getGroupId(JsonNode ticketNode, List<FSGroup> groups) {
        String type = ticketNode.get("type").asText();
        if(type.equals("laptop"))
             return groups.stream().filter(d -> "Hardware Team".equalsIgnoreCase(d.getName())).findFirst().get().getId();;
         if(type.equals("CustomerId"))
            return groups.stream().filter(d -> "Helpdesk Monitoring Team".equalsIgnoreCase(d.getName())).findFirst().get().getId();;

        return 29000592980L;
    }


    private static Long departmentId(JsonNode ticketNode, List<FSDepartment> departments) {
        String type = ticketNode.get("type").asText();

        if(type.equals("laptop"))
            return departments.stream().filter(d -> "IT".equalsIgnoreCase(d.getName())).findFirst().get().getId();
        else if(type.equals("CustomerId"))
            return departments.stream().filter(d -> "Operations".equalsIgnoreCase(d.getName())).findFirst().get().getId();

        return 29000592980L;
    }

    static ConnectorData getConnectorData(Map<String, Object> workflowInput){

        String jsonInput = JsonUtil.toJsonString(workflowInput.get("create_fs_ticket_connector"));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ConnectorData request = objectMapper.readValue(jsonInput, ConnectorData.class);

            Map<String, Object> jsonMap = JsonUtil.parseAsObject(jsonInput, new TypeReference<>() {
            });
            return request;
//              BambooHrConnectorData.builder()
//                      .user(String.valueOf(Optional.ofNullable(jsonMap.get("user")).orElse("testSR")))
//                      .password(String.valueOf(Optional.ofNullable(jsonMap.get("password")).orElse("dbe5b4ebd227cc1006ef9135b79ee8e7b8795ad7")))
//                      .op(String.valueOf(Optional.ofNullable(jsonMap.get("op")).orElse("create")))
//                      .operationData(String.valueOf(Optional.ofNullable(jsonMap.get("op")).orElse("create")))
//                      .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
