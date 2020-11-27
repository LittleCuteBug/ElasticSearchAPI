package Dyno.ElasticSearchAPI;

import net.minidev.json.JSONArray;
import org.json.JSONObject;

public class StatusCodeReturn {
    public static String StatusCode(String code, String mes, String status){
        JSONObject obj = new JSONObject();
        obj.put("data",new JSONArray());
        obj.put("code",code);
        obj.put("status",status);
        obj.put("message",mes);
        obj.put("search_time","0m");
        obj.put("total",0);
        return obj.toString();
    }
}
