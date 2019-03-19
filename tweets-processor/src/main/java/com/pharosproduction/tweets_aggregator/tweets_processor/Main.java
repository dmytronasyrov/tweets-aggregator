package com.pharosproduction.tweets_aggregator.tweets_processor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Main {

  private static final String STREAM_TWEETS_RAW = "streaming.tweets.raw";

  private static final Logger sLogger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    System.out.println("AAAAA");

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.enableCheckpointing(5000);
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

    final ParameterTool params = ParameterTool.fromArgs(args);
    env.getConfig().setGlobalJobParameters(params);
    System.out.println("BBBBB");

    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "tweets.raw");
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    System.out.println("CCCCC");

    FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(
      STREAM_TWEETS_RAW,
      new SimpleStringSchema(),
      properties
    );
    System.out.println("DDDDDD");

    DataStream<String> stream = env.addSource(consumer);
    stream.rebalance().map((MapFunction<String, Tuple2<String, String>>) value -> {
      JsonObject json = new JsonParser()
        .parse(value)
        .getAsJsonObject();

      String tweetId = json.get("id_str").getAsString();
      String tweetText = json.get("text").getAsString();
//        sLogger.info("Porcessing: " + value);

      return new Tuple2<>(tweetId, tweetText);
    })
    AsyncFunction<RegisterRequest, RegisterResponse> loginRestTransform =
      new AsyncRegisterApiInvocation(apiTimeoutMs);

    //Transform the datastream in parallel
    DataStream<RegisterResponse> result = AsyncDataStream
      .unorderedWait(messageStream, loginRestTransform, 5000, TimeUnit.MILLISECONDS, 1)
      .setParallelism(1);

    Properties producerProp = FlinkKafkaProducerConfig.getKafkaProduerConfig();

    //Write the result back to the Kafka sink i.e response topic
    result.addSink(new FlinkKafkaProducer<>(producerProp.getProperty("topic"), new RegisterResponseSerializer(),
      producerProp));
    env.execute("Streaming tweets");

  }
}
