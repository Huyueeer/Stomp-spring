### Amqp高级消息队列

#### 基于RabbitMQ的队列概念

相比kafka而言，RabbitMQ拥有交换机和队列的概念，拥有以下4种概念

1. 直接交换机（Direct）
2. 扇形交换机（Funout）
3. 主题交换机（Topic）
4. 头交换机（Header）

#### 消息回调机制（重要）

1. **生产者回调**

   顾名思义：就是生产数据的一方对当前数据是否已经成功加入到队列中的一种反馈；

   分为4种情况：

   1. 找不到交换机
   2. 找不到队列
   3. 都找不到
   4. 成功

   **在SpringBoot项目上需要去开启对应的配置**

   1. application.yml

      ```yml
      spring: 
      	rabbitmq:
      		#确认消息已发送到交换机(Exchange)
          publisher-confirms: true
          #确认消息已发送到队列(Queue)
          publisher-returns: true
      ```

   2. 配置类, 需要注意的是新版本的加入`setReturnCallback`方法中重写的入参改变了

      ```java
      @Bean
          public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory){
              RabbitTemplate rabbitTemplate = new RabbitTemplate();
              rabbitTemplate.setConnectionFactory(connectionFactory);
              //设置开启Mandatory,才能触发回调函数,无论消息推送结果怎么样都强制调用回调函数
              rabbitTemplate.setMandatory(true);
      
              rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
                  @Override
                  public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                      System.out.println("ConfirmCallback:     "+"相关数据："+correlationData);
                      System.out.println("ConfirmCallback:     "+"确认情况："+ack);
                      System.out.println("ConfirmCallback:     "+"原因："+cause);
                  }
              });
      
              rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
                  @Override
                  public void returnedMessage(ReturnedMessage returnedMessage) {
                      System.out.println("ReturnCallback:     "+"消息："+returnedMessage.getMessage());
                      System.out.println("ReturnCallback:     "+"回应码："+returnedMessage.getReplyCode());
                      System.out.println("ReturnCallback:     "+"回应信息："+returnedMessage.getReplyText());
                      System.out.println("ReturnCallback:     "+"交换机："+returnedMessage.getMessage());
                      System.out.println("ReturnCallback:     "+"路由键："+returnedMessage.getRoutingKey());
                  }
              });
      
              return rabbitTemplate;
          }
      ```

2. **消费者回调**

   理解意义，消费者去消费队列中的数据，当数据拿出来到达服务器后服务器发生错误，那么回调就可以去调节失败消费的解决办法，可以将此消息重新入队，下次消费等等；

   代码的实现：ConsumerConfig

   ```java
   @Configuration
   public class ConsumerConfig {
   
       @Autowired
       private CachingConnectionFactory connectionFactory;
   
       @Autowired
       private MyAckReceiver myAckReceiver;//消息接收处理类
   
       @Bean
       public SimpleMessageListenerContainer simpleMessageListenerContainer() {
           SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
           container.setConcurrentConsumers(1);
           container.setMaxConcurrentConsumers(1);
           container.setAcknowledgeMode(AcknowledgeMode.MANUAL); // RabbitMQ默认是自动确认，这里改为手动确认消息
           //设置一个队列
           //container.setQueueNames("TestDirectQueue");
           //如果同时设置多个如下： 前提是队列都是必须已经创建存在的
           //  container.setQueueNames("TestDirectQueue","TestDirectQueue2","TestDirectQueue3");
   
   
           //另一种设置队列的方法,如果使用这种情况,那么要设置多个,就使用addQueues
           //container.setQueues(new Queue("TestDirectQueue",true));
           //container.addQueues(new Queue("TestDirectQueue2",true));
           //container.addQueues(new Queue("TestDirectQueue3",true));
           container.setMessageListener(myAckReceiver);
   
           return container;
       }
   
   }
   ```

   那么在处理回调时就需要自己去写一个关于手动回调的类；

   ```java
   @Component
   public class MyAckReceiver implements ChannelAwareMessageListener {
       @Override
       public void onMessage(Message message, Channel channel) throws Exception {
           long deliveryTag = message.getMessageProperties().getDeliveryTag();
           try {
               //因为传递消息的时候用的map传递,所以将Map从Message内取出需要做些处理
               String msg = message.toString();
               String[] msgArray = msg.split("'");//可以点进Message里面看源码,单引号直接的数据就是我们的map消息数据
               Map<String, String> msgMap = mapStringToMap(msgArray[1].trim(),3);
               String messageId=msgMap.get("messageId");
               String messageData=msgMap.get("messageData");
               String createTime=msgMap.get("createTime");
   
               if("queue1".equals(message.getMessageProperties().getConsumerQueue())){
                   //对应不同队列的手动消费需求
               }
   
               System.out.println("  MyAckReceiver  messageId:"+messageId+"  messageData:"+messageData+"  createTime:"+createTime);
               System.out.println("消费的主题消息来自："+message.getMessageProperties().getConsumerQueue());
               channel.basicAck(deliveryTag, true); //第二个参数，手动确认可以被批处理，当该参数为 true 时，则可以一次性确认 delivery_tag 小于等于传入值的所有消息
   //			channel.basicReject(deliveryTag, true);//第二个参数，true会重新放回队列，所以需要自己根据业务逻辑判断什么时候使用拒绝
           } catch (Exception e) {
               channel.basicReject(deliveryTag, false);
               e.printStackTrace();
           }
       }
   
       //{key=value,key=value,key=value} 格式转换成map
       private Map<String, String> mapStringToMap(String str,int entryNum ) {
           str = str.substring(1, str.length() - 1);
           String[] strs = str.split(",",entryNum);
           Map<String, String> map = new HashMap<String, String>();
           for (String string : strs) {
               String key = string.split("=")[0].trim();
               String value = string.split("=")[1];
               map.put(key, value);
           }
           return map;
       }
   }
   ```

   包括监听多个队列也可以通过`message.getMessageProperties().getConsumerQueue()`来制定不同队列的解决方案。

#### 参考资料

1. RabbitMQ官网

2. [http://rabbitmq.mr-ping.com/]:RabbitMQ瓶先生中文文档

3. [ https://blog.csdn.net/qq_35387940/article/details/100514134]:优质CSND博文

