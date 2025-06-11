package cn.ksmcbrigade.ailocalizer.config;

import cn.ksmcbrigade.ailocalizer.Constants;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class Config {
    public final File file;
    public String notice = "Please get and copy the account token into there in https://cloud.siliconflow.cn/account/ak.,model: Qwen/Qwen2.5-7B-Instruct";
    public String apiKey = "";
    public String englishFile = "en_us.json";
    public String chineseFile = "zh_cn.json";
    public String reconnectTimes = "5";
    public String delay = "true";

    public Config(File file) throws IOException, IllegalAccessException {
        this.file = file;
        save(false);
        read();
    }

    public void save(boolean e) throws IllegalAccessException, IOException {
        if(!file.exists() || e){
            JsonObject object = new JsonObject();
            for (Field declaredField : this.getClass().getDeclaredFields()) {
                if(declaredField.getType().equals(String.class)){
                    object.addProperty(declaredField.getName(), (String) declaredField.get(this));
                }
            }
            FileUtils.writeStringToFile(file,object.toString());
        }
    }

    public void read() throws IllegalAccessException, IOException {
        JsonObject object = JsonParser.parseString(FileUtils.readFileToString(file)).getAsJsonObject();
        for (Field declaredField : this.getClass().getDeclaredFields()) {
            if(declaredField.getType().equals(String.class) && object.keySet().contains(declaredField.getName())){
                declaredField.set(this,object.get(declaredField.getName()).getAsString());
            }
        }
    }

    public boolean delay(){
        return Boolean.parseBoolean(delay);
    }

    public int rec_times() throws IOException, IllegalAccessException {
        try {
            return Integer.parseInt(reconnectTimes);
        } catch (NumberFormatException e) {
            reconnectTimes = "5";
            save(true);
            Constants.LOG.info("Has already reset the config value: {}","reconnectTimes");
            return 5;
        }
    }
}
