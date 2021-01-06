package com.huyue.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DirectConsumer {

    @RabbitListener(queues = "TestDirectQueue")
    public void process(String testMessage){
        System.out.println("DirectReceiver消费：" + testMessage);
    }
}
