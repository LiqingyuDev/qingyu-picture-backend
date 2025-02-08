package com.qingyu.qingyupicturebackend.api.imagesearch.sub;

import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取图片列表页面的首个图片 URL 的 API 类。
 *
 * @author qingyu
 * @date 2025/1/22 19:06
 */
@Slf4j
public class GetImageFirstUrlApi {

    /**
     * 获取图片列表页面的首个图片 URL。
     *
     * @param url 请求的目标 URL
     * @return 提取到的 firstUrl 链接
     * @throws BusinessException 如果提取失败或未找到符合条件的 firstUrl
     */
    public static String getImageFirstUrl(String url) {
        try {
            // 使用 Jsoup 获取 HTML 内容
            Document document = fetchDocument(url);

            // 从文档中提取包含 "firstUrl" 的脚本内容
            return extractFirstUrlFromDocument(document);
        } catch (BusinessException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    /**
     * 使用 Jsoup 连接到指定 URL 并获取 HTML 文档。
     *
     * @param url 请求的目标 URL
     * @return 解析后的 HTML 文档
     * @throws Exception 如果连接或解析失败
     */
    private static Document fetchDocument(String url) throws Exception {
        return Jsoup.connect(url)
                .timeout(5000) // 设置超时时间为5秒
                .get();
    }

    /**
     * 从 HTML 文档中提取包含 "firstUrl" 的脚本内容，并使用正则表达式匹配 firstUrl 的值。
     *
     * @param document 解析后的 HTML 文档
     * @return 提取到的 firstUrl 链接
     * @throws BusinessException 如果未找到符合条件的 firstUrl
     */
    private static String extractFirstUrlFromDocument(Document document) {
        Elements scriptElements = document.getElementsByTag("script");

        for (Element script : scriptElements) {
            String scriptContent = script.html();
            if (scriptContent.contains("\"title\":\"相似图片\"") && scriptContent.contains("\"firstUrl\"")) {
                // 使用正则表达式提取 firstUrl 的值
                Pattern pattern = Pattern.compile("\"title\"\\s*:\\s*\"相似图片\".*?\"firstUrl\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(scriptContent);
                if (matcher.find()) {
                    String firstUrl = matcher.group(1).replace("\\/", "/");
                    log.info("成功提取到 firstUrl: {}", firstUrl);
                    return firstUrl;
                }
            }
        }

        log.warn("未找到符合条件的 firstUrl");
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "未找到符合条件的 firstUrl");
    }

    /**
     * 主方法，用于测试 {@link #getImageFirstUrl(String)} 方法。
     */
    public static void main(String[] args) {
        // 请求目标 URL
        String url = "https://graph.baidu.com/s?card_key=&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&f=all&isLogoShow=1&session_id=16934843548880130471&sign=126417d07e2b5a9e315ea01737522594&tpl_from=pc";

        try {
            String imageFirstUrl = getImageFirstUrl(url);
            System.out.println("搜索成功，结果 URL：" + imageFirstUrl);
        } catch (BusinessException e) {
            System.err.println("操作失败: " + e.getMessage());
        }
    }
}
