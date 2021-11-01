package ru.gpn.etranintegration.model.ibpd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LastOperDateResponse {

    @JsonProperty("last_oper_date")
    private String lastOperDate;

}
