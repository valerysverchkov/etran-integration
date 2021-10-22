package ru.gpn.etranintegration.model.etran;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    public ObjectFactory() {
    }

    public GetBlockRequest createGetBlockRequest() {
        return new GetBlockRequest();
    }

    public GetBlockResponse createGetBlockResponse() {
        return new GetBlockResponse();

    }
}
