package com.zuperglue.bus.services.subscription;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by zuperglue on 2017-09-22.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionRequest {
    String callback;

    public SubscriptionRequest(){}
    public SubscriptionRequest(String callback){
        this.callback = callback;
    }
    public String getCallback() {
        return callback;
    }
}
