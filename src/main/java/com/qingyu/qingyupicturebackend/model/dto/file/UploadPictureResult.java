package com.qingyu.qingyupicturebackend.model.dto.file;

import com.qingyu.qingyupicturebackend.model.vo.UserVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/18 下午8:51
 */
@Data
public class UploadPictureResult implements Serializable {

    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片名称
     */
    private String picName;


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


    private static final long serialVersionUID = 1L;
}
