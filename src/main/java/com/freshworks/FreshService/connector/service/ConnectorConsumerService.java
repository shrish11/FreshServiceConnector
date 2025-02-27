package com.freshworks.FreshService.connector.service;

import java.util.Map;
import com.freshworks.FreshService.connector.request.TicketConnectorRequest;
import com.freshworks.FreshService.hagrid.assets.*;
import java.util.List;
import com.freshworks.freshindex.index.query.Expression;
import com.freshworks.core.shared.consumer.AssetStreamResponse;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;
import java.util.ArrayList;

@Slf4j
@Service
public class ConnectorConsumerService extends AbstractConsumerService{


    @Override
     public List<Ticket> consumeTicketAsset(TicketConnectorRequest connectorRequest , ConnectorHagridConfiguration connectorHagridConfiguration) throws Exception{

        log.info("consumeTicketAsset invoked");
        return connectorHagridConfiguration.getConsumerService().getAssetByAssetType(Ticket.class);

     }


     @Override
      public List<Ticket> consumeTicketAssetByFilter(TicketConnectorRequest connectorRequest , ConnectorHagridConfiguration connectorHagridConfiguration , Expression expression) throws Exception{

           log.info("consumeTicketAssetByFilter invoked");
           return connectorHagridConfiguration.getConsumerService().getAssetByAssetTypeAndFilter(Ticket.class , expression);

      }


       @Override
        public List<Ticket> consumeTicketAssetStream(TicketConnectorRequest connectorRequest , ConnectorHagridConfiguration connectorHagridConfiguration , int startToken , int endToken) throws Exception{

            log.info("consumeTicketAssetStream invoked");
             AssetStreamResponse.Token ticketToken = new AssetStreamResponse.Token();
             ticketToken.setStart(startToken);
             ticketToken.setCount(endToken);
             AssetStreamResponse<Ticket> ticketAssetStreamResponse = connectorHagridConfiguration.getConsumerService().streamAssetByAssetType(Ticket.class, ticketToken);
               if(Objects.isNull(ticketAssetStreamResponse.getNextToken()))
                     return new ArrayList<>();
             return ticketAssetStreamResponse.getAbstractAssetList();

        }

}
