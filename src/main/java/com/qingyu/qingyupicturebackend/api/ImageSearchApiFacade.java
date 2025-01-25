package com.qingyu.qingyupicturebackend.api;

import com.qingyu.qingyupicturebackend.api.model.ImageSearchResult;
import com.qingyu.qingyupicturebackend.api.sub.GetImageFirstUrlApi;
import com.qingyu.qingyupicturebackend.api.sub.GetImageListApi;
import com.qingyu.qingyupicturebackend.api.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author qingyu
 * @description 以图搜图接口门面
 * @date 2025/1/23 20:22
 */

@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索相似图
     */
    public static List<ImageSearchResult> searchSimilarImage(String imageUrl) {
        //通过图片链接获取以图搜图页面url
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        // 通过以图搜图页面url获取first url(Json)
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        // 通过first url获取图片列表
        List<ImageSearchResult> imageSearchResults = GetImageListApi.parseImageUrl(imageFirstUrl);

        return imageSearchResults;
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        String imageUrl = "https://tse3-mm.cn.bing.net/th/id/OIP-C.2PKgvFf7HJFeV1FvfqlmhwHaHa?rs=1&pid=ImgDetMain";
        List<ImageSearchResult> imageSearchResults = searchSimilarImage(imageUrl);
        for (ImageSearchResult imageSearchResult : imageSearchResults) {
            System.out.println(imageSearchResult);
        }
    }
}
