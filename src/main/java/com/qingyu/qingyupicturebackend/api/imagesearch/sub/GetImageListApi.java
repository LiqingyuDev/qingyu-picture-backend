package com.qingyu.qingyupicturebackend.api.imagesearch.sub;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qingyu.qingyupicturebackend.api.imagesearch.sub.model.ImageSearchResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取图片列表的API类。
 *
 * @author qingyu
 * @date 2025/1/22 20:28
 */
@Slf4j
public class GetImageListApi {

    /**
     * 解析 JSON 字符串并提取所有 "fromUrl" 和 "thumbUrl"。
     *
     * @param url 请求的目标 URL
     * @return 包含 {@link ImageSearchResult} 对象的列表
     */
    public static List<ImageSearchResult> parseImageUrl(String url) {
        // 发起 GET 请求并获取响应
        HttpResponse response = HttpUtil.createGet(url).execute();
        int statusCode = response.getStatus();
        String body = response.body();

        log.info("接口调用成功，响应码: {}, 响应内容: {}", statusCode, body);

        // 将响应体解析为 JSON 对象
        JSONObject jsonObject = JSONUtil.parseObj(body);

        // 获取 data 列表
        JSONArray dataList = jsonObject.getJSONObject("data").getJSONArray("list");
        ArrayList<ImageSearchResult> results = new ArrayList<>();

        if (dataList != null) {
            for (Object item : dataList) {
                JSONObject imageItem = (JSONObject) item;

                // 创建 ImageSearchResult 实例并设置属性
                ImageSearchResult result = new ImageSearchResult();
                result.setThumbUrl(imageItem.getStr("thumbUrl"));
                result.setFromUrl(imageItem.getStr("fromUrl"));

                results.add(result);
            }
        } else {
            log.warn("data.list is null or not found in the JSON string.");
        }

        return results;
    }

    /**
     * 主方法，用于测试 {@link #parseImageUrl(String)} 方法。
     */
    public static void main(String[] args) {
        String url = "https://graph.baidu.com/ajax/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&inspire=general_pc&limit=30&next=2&render_type=card&session_id=16709916879831538679&sign=126ac7d07e2b5a9e315ea01743651680&tk=218b3&tpl_from=pc";

        try {
            List<ImageSearchResult> resultList = parseImageUrl(url);
            System.out.println("搜索成功");

            // 打印结果以验证
            resultList.forEach(result ->
                    log.info("Thumb URL: {}, From URL: {}", result.getThumbUrl(), result.getFromUrl())
            );
        } catch (Exception e) {
            log.error("解析图片列表失败", e);
        }
    }
}
