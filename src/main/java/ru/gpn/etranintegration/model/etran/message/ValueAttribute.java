package ru.gpn.etranintegration.model.etran.message;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ValueAttribute {

    @JacksonXmlProperty(isAttribute = true, localName = "value")
    @XmlAttribute(name = "value")
    private String value;

}
