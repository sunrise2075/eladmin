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

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.sql.Timestamp;
import java.io.Serializable;
import java.util.List;

/**
* @website https://el-admin.vip
* @description /
* @author tiger
* @date 2021-07-24
**/
@Data
public class WorksInfoDto implements Serializable {

    /** 自增主键 */
    private Integer id;

    /** 作者姓名 */
    private String authorName;

    /** 作者手机号 */
    private String authorMobile;

    /** 作品描述 */
    private String selfDescription;

    /**
     * 头像url
     * */
    private String headImgUrl;


    /**
     * 是否获奖: 0 -  没有获奖    1 -  已经获奖
     *
     * */
    private Integer winFlag;

    /**
     *
     * 是否最佳 ： 默认值 0 表示该作品不是最佳作品, 数字越大代表当前作品越佳
     *
     * */
    private Integer starIndex;

    /**
     *  得票总数
     * */
    private Integer voteCount;

    /** 作品种类: 0. 文字类  1. 图片类  2. 视频类 */
    private Integer type;

    /** 作品状态：1. 提交  2. 审核中  3. 审核通过  4. 审核拒绝
5. 获奖 */
    private Integer lifeStatus;

    private String articleContent;

    private List<String> files;

    /** 创建日期 */
    private Timestamp createdDate;

    /** 创建人 */
    private String createdBy;

    /** 更新人 */
    private String updatedBy;

    /** 更新日期 */
    private Timestamp updatedDate;
}