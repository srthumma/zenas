package tcloud;

import org.apache.spark.api.java.JavaSparkContext;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by nancy on 3/15/2015.
 */
public class TCloudMessageHandler{

    HashMap<HANDLER_TYPE,IMessageHandler> mHandlers = new HashMap<HANDLER_TYPE, IMessageHandler>();

    public TCloudMessageHandler(JavaSparkContext sc) {
          mHandlers.put(HANDLER_TYPE.ELASTIC_SEARCH,new ESMessageHandler(sc));
    }

    enum HANDLER_TYPE{
         ELASTIC_SEARCH
    }

    public void execute(String input){
        Iterator<HANDLER_TYPE> it = mHandlers.keySet().iterator();
        while (it.hasNext()) {
            mHandlers.get(it.next()).process(input);
        }
    }


}
