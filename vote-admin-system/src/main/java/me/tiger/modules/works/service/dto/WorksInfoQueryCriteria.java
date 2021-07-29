/*
*  Copyright 2019-2020 Zheng Jie
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package me.tiger.modules.works.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;
import me.tiger.annotation.Query;

/**
* @website https://el-admin.vip
* @author tiger
* @date 2021-07-24
**/
@Data
@ApiModel(value = "作品信息查询条件")
public class WorksInfoQueryCriteria{

    @ApiModelProperty(value = "作品类型: 0. 文字类  1. 图片类  2. 视频类", dataType = "integer", allowableValues = "0,1,2")
    private Integer type;

    @ApiModelProperty(value = "作者手机号")
    private String authorMobile;

    @ApiModelProperty(value = "作者姓名")
    private String authorName;
}