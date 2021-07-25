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
package me.tiger.modules.works.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.annotations.ApiModelProperty;
import cn.hutool.core.bean.copier.CopyOptions;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.sql.Timestamp;
import java.io.Serializable;

/**
* @website https://el-admin.vip
* @description /
* @author tiger
* @date 2021-07-24
**/
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="works_info")
public class WorksInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ApiModelProperty(value = "自增主键")
    private Integer id;

    @Column(name = "author_name")
    @ApiModelProperty(value = "作者姓名")
    private String authorName;

    @Column(name = "author_mobile")
    @ApiModelProperty(value = "作者手机号")
    private String authorMobile;

    @Column(name = "self_description")
    @ApiModelProperty(value = "作品描述")
    private String selfDescription;

    @Column(name = "type")
    @ApiModelProperty(value = "作品种类: 0. 文字类  1. 图片类  2. 视频类")
    private Integer type;

    @Column(name = "life_status")
    @ApiModelProperty(value = "作品状态：1. 提交  2. 审核中  3. 审核通过  4. 审核拒绝 5. 获奖")
    private Integer lifeStatus;

    @Column(name = "created_date")
    @ApiModelProperty(value = "创建日期")
    private Timestamp createdDate;

    @Column(name = "created_by")
    @ApiModelProperty(value = "创建人")
    private String createdBy;

    @Column(name = "updated_by")
    @ApiModelProperty(value = "更新人")
    private String updatedBy;

    @Column(name = "updated_date")
    @ApiModelProperty(value = "更新日期")
    private Timestamp updatedDate;

    public void copy(WorksInfo source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}