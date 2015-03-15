package tcloud;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 * Created by nancy on 3/15/2015.
 */
public class TCloudConfigProvider {
    private final Properties configProp = new Properties();

    private TCloudConfigProvider()
    {
        //Private constructor to restrict new instances
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("eventManager.properties");
        System.out.println("Read all properties from file");
        try {
            configProp.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static class LazyHolder
    {
        private static final TCloudConfigProvider INSTANCE = new TCloudConfigProvider();
    }

    public static TCloudConfigProvider getInstance()
    {
        return LazyHolder.INSTANCE;
    }

    public String get(String key){
        return configProp.getProperty(key);
    }

    public Set<String> getAllPropertyNames(){
        return configProp.stringPropertyNames();
    }

    public boolean containsKey(String key){
        return configProp.containsKey(key);
    }
}
