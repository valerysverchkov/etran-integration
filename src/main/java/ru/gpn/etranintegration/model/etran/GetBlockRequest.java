package ru.gpn.etranintegration.model.etran;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@Data
@XmlRootElement(name = "GetBlockRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"login", "password", "message"})
public class GetBlockRequest {

    @XmlElement(name = "Login")
    @JsonProperty("Login")
    private String login;

    @XmlElement(name = "Password")
    @JsonProperty("Password")
    private String password;

    @XmlElement(name = "Text")
    @JsonProperty("Text")
    private String message;

}
