package com.qingyu.qingyupicturebackend.api.imagesearch.sub;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author qingyu
 * @description 通过图片链接获取以图搜图页面url
 * @date 2025/1/21 17:10
 */
@Slf4j
public class GetImagePageUrlApi {

    public static String getImagePageUrl(String imageUrl) {
        // 1. 准备请求参数
        Map<String, Object> formParams = new HashMap<>();
        formParams.put("image", imageUrl);
        formParams.put("tn", "pc");
        formParams.put("from", "pc");
        formParams.put("image_source", "PC_UPLOAD_URL");

        // 构建请求的目标地址
        long uptime = System.currentTimeMillis();
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;

        try {
            // 2. 发送请求
            try (HttpResponse response = HttpRequest.post(url)
                    .form(formParams)
                    .execute()) {

                // 3. 处理响应
                if (response.isOk()) {
                    String responseBody = response.body();
                    log.info("从服务器获取的响应: {}", responseBody);

                    // 解析JSON响应
                    JSONObject jsonObject = JSONUtil.parseObj(responseBody);
                    if (jsonObject.getInt("status") == 0) {
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        if (dataObject != null) {
                            return dataObject.getStr("url");
                        } else {
                            log.error("响应中未找到 'data' 字段: {}", responseBody);
                        }
                    } else {
                        log.error("获取图片页面URL失败。响应消息: {}", jsonObject.getStr("msg"));
                    }
                } else {
                    log.error("获取图片页面URL失败。响应码: {}", response.getStatus());
                }
            }
        } catch (Exception e) {
            log.error("获取图片页面URL时发生错误", e);
        }
        return null;
    }

    public static void main(String[] args) {
        String imageUrl = "http://mms1.baidu.com/it/u=4036065385,824281058&fm=253&app=138&f=JPEG?w=100&h=100";
        String imagePageUrl = getImagePageUrl(imageUrl);
        System.out.println("图片页面URL: " + "\"" + imagePageUrl + "\"");
    }
}
