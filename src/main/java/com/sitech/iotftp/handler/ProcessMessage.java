package com.sitech.iotftp.handler;

import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;

public interface ProcessMessage {

    void process(JSONObject params);

}
