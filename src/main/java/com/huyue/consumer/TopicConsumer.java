package com.huyue.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TopicConsumer {

    @RabbitListener(queues = "topic.man")
    public void manProcess(Map testMessage) {
        System.out.println("TopicManReceiver消费者收到消息  : " + testMessage.toString());
    }

    @RabbitListener(queues = "topic.woman")
    public void womanProcess(Map testMessage) {
        System.out.println("TopicToTalReceiver消费者收到消息  : " + testMessage.toString());
    }
}
