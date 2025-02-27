package com.freshworks.FreshService.connector.service;

import com.freshworks.core.shared.consumer.ConsumerService;
import com.freshworks.FreshService.connector.request.TicketConnectorRequest;
import com.freshworks.FreshService.hagrid.assets.*;
import java.util.List;
import com.freshworks.freshindex.index.query.Expression;

public abstract class AbstractConsumerService {


        public abstract List<Ticket> consumeTicketAsset(TicketConnectorRequest connectorRequest , ConnectorHagridConfiguration connectorHagridConfiguration) throws Exception;


         public abstract List<Ticket> consumeTicketAssetByFilter(TicketConnectorRequest connectorRequest , ConnectorHagridConfiguration connectorHagridConfiguration , Expression expression) throws Exception;


         public abstract List<Ticket> consumeTicketAssetStream(TicketConnectorRequest connectorRequest , ConnectorHagridConfiguration connectorHagridConfiguration , int startToken , int endToken) throws Exception;


}
