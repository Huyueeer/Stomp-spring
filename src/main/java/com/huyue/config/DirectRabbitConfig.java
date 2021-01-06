package com.huyue.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectRabbitConfig {

    /**
     * 功能描述: 队列
     * @param
     * @return {@link Queue}
     * @author HuYueeer
     * @date 2021/1/5 10:07 下午
     */
    @Bean
    public Queue TestDirectQueue(){
        return new Queue("TestDirectQueue",true);
    }

    /**
     * 功能描述: 交换机
     * @param
     * @return {@link DirectExchange}
     * @author HuYueeer
     * @date 2021/1/5 10:12 下午
     */
    @Bean
    DirectExchange TestDirectExchange(){
        return new DirectExchange("TestDirectExchange",true,false);
    }

    /**
     * 功能描述: 绑定队列和交换机
     * @param
     * @return {@link Binding}
     * @author HuYueeer
     * @date 2021/1/5 10:21 下午
     */
    @Bean
    Binding bindingDirect() {
        return BindingBuilder.bind(TestDirectQueue())
                .to(TestDirectExchange())
                .with("TestDirectRouting");
    }

    @Bean
    DirectExchange lonelyDirectExchange(){
        return new DirectExchange("lonelyDirectExchange");
    }
}
