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
        String acsToken = "jmM4zyI8OUixvSuWh0sCy4xWbsttVMZb9qcRTmn6SuNWg0vCO7N0s6Lffec+IY5yuqHujHmCctF9BVCGYGH0H5SH/H3VPFUl4O4CP1jp8GoAzuslb8kkQQ4a21Tebge8yhviopaiK66K6hNKGPlWt78xyyJxTteFdXYLvoO6raqhz2yNv50vk4/41peIwba4lc0hzoxdHxo3OBerHP2rfHwLWdpjcI9xeu2nJlGPgKB42rYYVW50+AJ3tQEBEROlg/UNLNxY+6200B/s6Ryz+n7xUptHFHi4d8Vp8q7mJ26yms+44i8tyiFluaZAr66/+wW/KMzOhqhXCNgckoGPX1SSYwueWZtllIchRdsvCZQ8tFJymKDjCf3yI/Lw1oig9OKZCAEtiLTeKE9/CY+Crp8DHa8Tpvlk2/i825E3LuTF8EQfzjcGpVnR00Lb4/8A";

        try {
            // 2. 发送请求
            try (HttpResponse response = HttpRequest.post(url)
                    .header("Acs-Token", acsToken)
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
