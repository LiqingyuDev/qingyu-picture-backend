package com.qingyu.qingyupicturebackend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 删除请求类
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/8 上午7:58
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}

