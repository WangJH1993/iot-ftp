
package com.sitech.iotftp.messaging;


@FunctionalInterface
public interface EventPublisher {

  void sendEventMessage(final String event);

}
