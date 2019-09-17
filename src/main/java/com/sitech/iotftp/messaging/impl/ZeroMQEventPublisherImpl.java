/*******************************************************************************
 * Copyright 2016-2017 Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * @microservice: core-data
 * @author: Jim White, Dell
 * @version: 1.0.0
 *******************************************************************************/

package com.sitech.iotftp.messaging.impl;

import com.google.gson.Gson;
import com.sitech.iotftp.messaging.EventPublisher;
import org.apache.log4j.Logger;
import org.zeromq.ZMQ;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class ZeroMQEventPublisherImpl implements EventPublisher {

  private static Logger logger = Logger.getLogger(ZeroMQEventPublisherImpl.class);

  private static final long PUB_UP_SLEEP = 1000;

  private String zeromqAddressPort;

  private ZMQ.Socket publisher;
  private ZMQ.Context context;

  {
    context = ZMQ.context(1);
  }

  //zeroMQ的socket链接不是线程安全的，所以需要加同步所锁
  @Override
  public synchronized void sendEventMessage(String event) {
    try {
      if (publisher == null)
        getPublisher();
      if (publisher != null) {
        publisher.send(event.getBytes());
      } else
        logger.error("zeroMQ链接创建失败");
    } catch (Exception e) {
      logger.error("图片你上传zeroMQ失败："+e.getMessage());
    }
  }

  public String getZeromqAddressPort() {
    return zeromqAddressPort;
  }

  public void setZeromqAddressPort(String zeromqAddressPort) {
    this.zeromqAddressPort = zeromqAddressPort;
  }

  private void getPublisher() {
    try {
      if (publisher == null) {
        publisher = context.socket(ZMQ.PUB);
        publisher.bind(zeromqAddressPort);
        Thread.sleep(PUB_UP_SLEEP);
      }
    } catch (Exception e) {
      logger.error("Unable to get a publisher.  Error:  " + e);
      publisher = null;
    }
  }

}
