package ru.gpn.etranintegration.model.etran;

import lombok.Data;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ValueAttribute {

    @XmlAttribute(name = "value")
    private String value;

}
