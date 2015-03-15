package tcloud;
/**
 * A Camel Application
 */
import com.google.common.collect.Lists;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class TCloudConsumer {
    private static final Pattern SPACE = Pattern.compile(" ");

    private TCloudConsumer() {
    }

    public static void main(String[] args) {


        SparkConf sparkConf = new SparkConf().setAppName("TCloudConsumer");

        // Create the context with a 1 second batch size
        final JavaSparkContext sc = new JavaSparkContext(sparkConf);
        JavaStreamingContext jsc = new JavaStreamingContext(sc, new Duration(2000));

        int numThreads = Integer.parseInt(TCloudConfigProvider.getInstance().get("consumer.thread.count"));
        Map<String, Integer> topicMap = new HashMap<String, Integer>();
        String[] topics = TCloudConfigProvider.getInstance().get("consumer.topic").split(",");
        for (String topic: topics) {
            topicMap.put(topic, numThreads);
        }
        final TCloudMessageHandler messageHandler = new TCloudMessageHandler(sc);
        JavaPairReceiverInputDStream<String, String> messages =
                KafkaUtils.createStream(jsc, TCloudConfigProvider.getInstance().get("consumer.zookeeper.zkQuorum"),
                        TCloudConfigProvider.getInstance().get("consumer.group"), topicMap);

        JavaDStream<String> lines = messages.map(new Function<Tuple2<String, String>, String>() {
            @Override
            public String call(Tuple2<String, String> tuple2) {
                return tuple2._2();
            }
        });

        JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(String x) {

                return Lists.newArrayList(SPACE.split(x));
            }
        });

        words.foreachRDD(new Function<JavaRDD<String>, Void>() {
            @Override
            public Void call(JavaRDD<String> rdd) throws Exception {
                if(rdd!=null)
                {   String strInput = "";
                    for (String str : rdd.collect()) {
                        strInput = strInput + str;
                       /* String jsoData = "{\"content\" : \"" + str +"\" ,\"index\" : \"" + str + "\"}";
                        System.out.println(jsoData);
                        JavaRDD<String> stringRDD = sc.parallelize(ImmutableList.of(jsoData));
                        JavaEsSpark.saveJsonToEs(stringRDD, "index/tcloud");*/

                    }
                    System.out.println(strInput);
                    if (!strInput.isEmpty()) {
                        messageHandler.execute(strInput);
                    }
                }
                return null;
            }

        });

        JavaPairDStream<String, Integer> wordCounts = words.mapToPair(
                new PairFunction<String, String, Integer>() {
                    @Override
                    public Tuple2<String, Integer> call(String s) {
                        return new Tuple2<String, Integer>(s, 1);
                    }
                }).reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });

        wordCounts.foreach(new Function2<JavaPairRDD<String,Integer>, Time, Void>() {

            @Override
            public Void call(JavaPairRDD<String, Integer> values,
                             Time time) throws Exception {
                values.foreach(new VoidFunction<Tuple2<String, Integer>>() {

                    @Override
                    public void call(Tuple2<String, Integer> tuple)
                            throws Exception {

                        System.out.println("------------------------------- Counter:" + tuple._1() + "," + tuple._2());


                    }} );

                return null;
            }});

        words.print();
        jsc.start();
        jsc.awaitTermination();
    }
}

