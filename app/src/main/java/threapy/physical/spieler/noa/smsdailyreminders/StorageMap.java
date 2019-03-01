package threapy.physical.spieler.noa.smsdailyreminders;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class StorageMap {
    private  static String PROPERTIES_FILE = "/smsReminders.properties";
    private Properties properties;


    public StorageMap(File filesDir)  {
        try {
            File file = new File(filesDir.getAbsolutePath() + PROPERTIES_FILE);
            if(!file.exists()){
                file.createNewFile();
            }
            properties = new Properties();
            properties.load(new FileInputStream(file));
        } catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    public String get(String key){
       return  properties.getProperty(key);
    }

    public void set(String key, String value){
         properties.setProperty(key, value);
    }



}
