package me.tiger.modules.works.constant;


import com.alibaba.fastjson.JSONObject;

public class ResponseConstant {

    public static final Integer SUCCESS = 1;
    public static final Integer FAIL = 0;

    public static JSONObject buildResult(int code, String message, Object data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", message);
        jsonObject.put("data", data);
        return jsonObject;
    }
}
