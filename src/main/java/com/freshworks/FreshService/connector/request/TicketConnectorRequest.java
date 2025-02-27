package com.freshworks.FreshService.connector.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;



@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true )
public class TicketConnectorRequest extends CommonConnectorRequest {

    @JsonProperty("data")
    JsonNode data;
}
