package com.lnu.qa.thirdlab.client;

import com.lnu.qa.thirdlab.generated.*;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class FruitsClient extends WebServiceGatewaySupport {

    public GetFruitResponse getFruit(String fruitId) {
        GetFruitRequest request = new GetFruitRequest();
        request.setId(fruitId);

        return (GetFruitResponse) getWebServiceTemplate()
                .marshalSendAndReceive(request);
    }

    public CreateFruitResponse createFruit(CreateFruitRequest request) {
        return (CreateFruitResponse) getWebServiceTemplate()
                .marshalSendAndReceive(request);
    }

    public UpdateFruitResponse updateFruit(UpdateFruitRequest request) {
        return (UpdateFruitResponse) getWebServiceTemplate()
                .marshalSendAndReceive(request);
    }

    public RemoveFruitResponse removeFruit(RemoveFruitRequest request) {
        return (RemoveFruitResponse) getWebServiceTemplate()
                .marshalSendAndReceive(request);
    }

}
