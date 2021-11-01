package ru.gpn.etranintegration.model.etran;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GetBlockResponse {

    @JsonProperty("Text")
    private Object message;

    @JsonProperty("return")
    private Boolean result;

}
