package ru.gpn.etranintegration.model.etran;

import lombok.Data;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@Data
@XmlRootElement(name = "GetBlockResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"message", "result"})
public class GetBlockResponse {

    @XmlElement(name = "Text")
    private String message;

    @XmlElement(name = "return")
    private Boolean result;

}
