package tcloud;

import org.apache.spark.api.java.JavaSparkContext;

/**
 * Created by nancy on 3/15/2015.
 */
public class BaseHandler {
    protected JavaSparkContext context;

    protected BaseHandler(JavaSparkContext sc) {
        context= sc;
    }
}
