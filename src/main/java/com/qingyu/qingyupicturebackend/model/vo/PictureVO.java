package com.qingyu.qingyupicturebackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.qingyu.qingyupicturebackend.model.entity.Picture;
import lombok.Data;

/**
 * 图片
 *
 * @TableName picture
 */
@Data
public class PictureVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;


    /**
     * 图片名称
     */
    private String picName;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（数据库中是JSON 数组,为了前端方便像前端发List）
     */
    private List<String> tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 额外返回的字段-创建用户信息
     */
    private UserVO user;

    private static final long serialVersionUID = 1L;

    /**
     * vo封装类转对象
     */
    public static Picture voToObj(PictureVO pictureVO) {
        if (pictureVO == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureVO, picture);
        //tags类型不同需要转换
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }

    /**
     * 对象转vo封装类
     */
    public static PictureVO objToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtil.copyProperties(picture, pictureVO);

        // 获取 tags 字符串
        String tagsStr = picture.getTags();
        if (tagsStr != null && !tagsStr.isEmpty()) {
            try {
                // 尝试将 tags 字符串解析为 JSONArray
                JSONArray jsonArray = JSONUtil.parseArray(tagsStr);
                // 将 JSONArray 转换为 List<String>
                List<String> tagsList = JSONUtil.toList(jsonArray, String.class);
                pictureVO.setTags(tagsList);
            } catch (Exception e) {
                // 处理解析异常，例如记录日志或设置默认值
                System.err.println("Failed to parse tags for picture: " + picture.getId() + ", error: " + e.getMessage());
                pictureVO.setTags(null); // 或者设置一个默认值
            }
        } else {
            // 如果 tags 字符串为空或为 null，设置默认值
            pictureVO.setTags(null); // 或者设置一个默认值
        }

        return pictureVO;
    }
/*    public static PictureVO objToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtil.copyProperties(picture, pictureVO);
        //tags类型不同需要转换
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;
    }*/
}