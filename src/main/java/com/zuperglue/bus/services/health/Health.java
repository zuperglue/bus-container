package com.zuperglue.bus.services.health;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by zuperglue on 2017-09-22.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Health {

    String status;

    public Health(String status){
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

}
