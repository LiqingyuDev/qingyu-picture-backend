package com.qingyu.qingyupicturebackend.model.dto.picture;

import com.qingyu.qingyupicturebackend.api.aliyunai.outPainting.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @description 创建图片扩图任务请求
 *
 * @author qingyu
 * @date 2025/2/4 20:15
 */
 
 @Data
 public class CreatePictureOutPaintingTaskRequest implements Serializable {

 /**
  * 图片 id
  */
 private Long pictureId;

 /**
  * 扩图参数
  */
 private CreateOutPaintingTaskRequest.Parameters parameters;

 private static final long serialVersionUID = 1L;
}
