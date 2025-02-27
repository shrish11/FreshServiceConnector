package com.freshworks.FreshService.connector.service;

import com.freshworks.FreshService.connector.request.TicketConnectorRequest;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public interface ConnectorService {

    ImmutableMap<String , String> filterAndCreateBaggageMap(Map<String,String> inputData);

    Map<String , Object> consume(TicketConnectorRequest connectorRequest , ConnectorHagridConfiguration connectorHagridConfiguration);



    void startSync(ImmutableMap<String,String> map , TicketConnectorRequest connectorRequest , ConnectorHagridConfiguration connectorHagridConfiguration);

    void clearData(ConnectorHagridConfiguration connectorHagridConfiguration);

    boolean isSyncComplete(ConnectorHagridConfiguration connectorHagridConfiguration);
}
