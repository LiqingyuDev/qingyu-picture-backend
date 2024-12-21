package com.qingyu.qingyupicturebackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/20 下午8:05
 */
@Data
public class PictureTagCategory implements Serializable {
    private List<String> tagList;
    private List<String> categoryList;
}
