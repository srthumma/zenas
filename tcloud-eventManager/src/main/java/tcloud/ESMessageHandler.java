package tcloud;

import com.google.common.collect.ImmutableList;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.elasticsearch.spark.rdd.api.java.JavaEsSpark;
import org.json.JSONObject;

/**
 * Created by nancy on 3/15/2015.
 */
public class ESMessageHandler extends BaseHandler implements IMessageHandler {


    protected ESMessageHandler(JavaSparkContext sc) {
        super(sc);
        context.getConf().set("es.index.auto.create",TCloudConfigProvider.getInstance().get("es.index.auto.create"));
        context.getConf().set("es.nodes",TCloudConfigProvider.getInstance().get("es.nodes"));
        context.getConf().set("es.port",TCloudConfigProvider.getInstance().get("es.port"));

    }

    @Override
    public void process(String input) {
        String jData = new JSONObject(input).toString();
        System.out.println(context.getConf());
        JavaRDD<String> stringRDD = context.parallelize(ImmutableList.of(jData));
        JavaEsSpark.saveJsonToEs(stringRDD, TCloudConfigProvider.getInstance().get("es.index.name"));
    }
}
