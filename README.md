# nsq-client
一个nsq的java client。
# Getting Started
## Artifact
``` java
<dependency>
  <groupId>com.github.haiger</groupId>
  <artifactId>nsq-client</artifactId>
  <version>1.1.0</version>
</dependency>
```
## Producer
Example usage:
``` java
String lookupHost = "192/168.1.1";
int lookupPort = 4161;
Pruducer producer = new Producer(lookupHost, lookupPort);
producer.connect();

// result == true meaning put success;
// catch exception meaning put fail;
boolean result = producer.put("testTopic", "some message".getBytes());

// safe close
Runtime.getRuntime().addShutdownHook(new Thread() {
    @Override
    public void run() {
        producer.close();
    }
});
```
## Consumer
Example usage:
``` java
String lookupHost = "192/168.1.1";
int lookupPort = 4161;

ConsumerHandler msgHandler = new ConsumerHandler() {
    @Override
    public void handle(byte[] msg) throws Exception {
        // deal the msg
        // msg can be auto-finish
        // throw execption can requeue the msg
    }
};

Consumer consumer = new Consumer(lookupHost, loopupPort, "testTopic", "testChannel");
consumer.setConsumerhandler(msgHandler);
consumer.connect();


// safe close
Runtime.getRuntime().addShutdownHook(new Thread() {
    @Override
    public void run() {
        consumer.close();
    }
});
```
具体示例可以参考test包中的example。

