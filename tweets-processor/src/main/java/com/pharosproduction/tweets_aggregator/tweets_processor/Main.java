package com.pharosproduction.tweets_aggregator.tweets_processor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Main {

  // Constants

  private static final String STREAM_TWEETS_RAW = "streaming.tweets.raw";

  // Variables

  private static final Logger sLogger = LoggerFactory.getLogger(Main.class);

  // Main

  public static void main(String[] args) throws Exception {
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.enableCheckpointing(5000);
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

    final ParameterTool params = ParameterTool.fromArgs(args);
    env.getConfig().setGlobalJobParameters(params);

    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "tweets.raw");
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

    FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(
      STREAM_TWEETS_RAW,
      new SimpleStringSchema(),
      properties
    );

    DataStream<String> stream = env.addSource(consumer);
    SingleOutputStreamOperator<Tuple2<String, String>> newStream = stream.rebalance()
      .map(new MapFunction<String, Tuple2<String, String>>() {
        @Override
        public Tuple2<String, String> map(String s) {
          JsonObject json = new JsonParser()
            .parse(s)
            .getAsJsonObject();

          String tweetId = json.get("id_str").getAsString();
          String tweetText = json.get("text").getAsString();

          return new Tuple2<>(tweetId, tweetText);
        }
      });

    Properties propertiesProducer = new Properties();
    propertiesProducer.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    propertiesProducer.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "tweets.raw");
    propertiesProducer.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    propertiesProducer.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    propertiesProducer.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    propertiesProducer.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

    FlinkKafkaProducer<Tuple2<String, String>> producer = new FlinkKafkaProducer<>("streaming.tweets", new RegisterResponseSerializer(), propertiesProducer);
    newStream.addSink(producer);

    env.execute("Streaming tweets");

  }

  private static class RegisterResponseSerializer implements KeyedSerializationSchema<Tuple2<String, String>> {

    private static final long serialVersionUID = 6154188370181669751L;

    @Override
    public byte[] serializeKey(Tuple2<String, String> stringStringTuple2) {
      return stringStringTuple2.f0.getBytes();
    }

    @Override
    public byte[] serializeValue(Tuple2<String, String> stringStringTuple2) {
      return stringStringTuple2.f1.getBytes();
    }

    @Override
    public String getTargetTopic(Tuple2<String, String> stringStringTuple2) {
      return "streaming.tweets";
    }
  }
}
