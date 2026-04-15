package com.example.demofirst.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * 敏感数据过滤工具类：替换密码、手机号等敏感字段
 */
public class SensitiveDataUtils {

    // 需要过滤的敏感字段列表
    private static final String[] SENSITIVE_FIELDS = {"password", "pwd", "mobile", "phone", "idCard", "idcard"};

    /**
     * 过滤JSON字符串中的敏感字段
     */
    public static String filterSensitiveData(String jsonStr) {
        try {
            Object jsonObj = JSON.parse(jsonStr);

            // 处理对象场景
            if (jsonObj instanceof JSONObject) {
                JSONObject obj = (JSONObject) jsonObj;
                for (String field : SENSITIVE_FIELDS) {
                    obj.put(field, "***");
                }
                return obj.toJSONString();
            }

            // 处理数组场景
            if (jsonObj instanceof JSONArray) {
                JSONArray arr = (JSONArray) jsonObj;
                for (int i = 0; i < arr.size(); i++) {
                    if (arr.get(i) instanceof JSONObject) {
                        JSONObject item = arr.getJSONObject(i);
                        for (String field : SENSITIVE_FIELDS) {
                            item.put(field, "***");
                        }
                    }
                }
                return arr.toJSONString();
            }

            // 非对象/数组类型（如字符串、数字），直接返回
            return jsonStr;
        } catch (Exception e) {
            return jsonStr;
        }
    }

    /**
     * 过滤对象中的敏感字段（转JSON后过滤）
     */
    public static String filterSensitiveData(Object obj) {
        if (obj == null) {
            return null;
        }
        String jsonStr = JSON.toJSONString(obj);
        return filterSensitiveData(jsonStr);
    }

}
