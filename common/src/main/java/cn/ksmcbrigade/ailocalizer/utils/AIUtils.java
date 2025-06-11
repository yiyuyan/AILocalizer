package cn.ksmcbrigade.ailocalizer.utils;

import cn.ksmcbrigade.ailocalizer.CommonClass;
import cn.ksmcbrigade.ailocalizer.Constants;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AIUtils {

    private static final URL url;
    public static int reconnectTimes = 0;

    static {
        try {
            url = new URL("https://api.siliconflow.cn/v1/chat/completions");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String transfer(String context,String key) throws IOException, IllegalAccessException {
        if(context.isEmpty()) return context;
        if(reconnectTimes > CommonClass.CONFIG.rec_times()){
            reconnectTimes = 0;
            Constants.LOG.error("Can't transfer it because the connect time out: {}",context);
            return context;
        }
        final HttpURLConnection connection = getHttpURLConnection(key);

        String output = "{\n  \"model\": \"Qwen/Qwen2.5-7B-Instruct\",\n  \"messages\": [\n    {\n      \"content\": \"transfer it to Chinese(these words from the Game Minecraft,please use some words in the game to transfer,only transfer),Do not provide feedback on texts that are not related to the translated content: {context}\",\n      \"role\": \"user\"\n    }\n  ]\n}".replace("{context}",context);

        try (OutputStream o = connection.getOutputStream()){
            byte[] input = output.getBytes(StandardCharsets.UTF_8);
            o.write(input, 0, input.length);
        }

        try {
            connection.connect();
            reconnectTimes = 0;
        } catch (SocketTimeoutException ex){
            Constants.LOG.info("Reconnecting...");
            reconnectTimes++;
            transfer(context,key);
        }

        if(connection.getResponseCode()==200){
            try {
                JsonObject ret;
                try(InputStream is = connection.getInputStream()){
                    ret = JsonParser.parseString(new String(is.readAllBytes(),StandardCharsets.UTF_8)).getAsJsonObject();
                }

                return ret.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
            } catch (Exception e) {
                Constants.LOG.error("Failed to read the text.",e);
                return context;
            }
        }
        else{
            try(InputStream ein = connection.getErrorStream()){
                Constants.LOG.error("Failed to transfer the text: {}",new String(ein.readAllBytes()));
            }
            return context;
        }
    }

    private static HttpURLConnection getHttpURLConnection(String key) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setConnectTimeout(5000000);
        connection.setReadTimeout(5000000);
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");

        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json; charset=UTF-8");
        connection.setRequestProperty("Authorization", "Bearer "+ key);

        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }
}
