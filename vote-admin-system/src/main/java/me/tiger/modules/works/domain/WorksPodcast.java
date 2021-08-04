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
import javax.persistence.*;
import javax.validation.constraints.*;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;
import java.sql.Timestamp;
import java.io.Serializable;

/**
* @website https://el-admin.vip
* @description /
* @author tiger
* @date 2021-07-31
**/
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name="works_podcast")
public class WorksPodcast implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ApiModelProperty(value = "id")
    private Integer id;

    @Column(name = "url")
    @ApiModelProperty(value = "直播链接地址")
    private String url;

    @Column(name = "image_path")
    @ApiModelProperty(value = "海报相对路径")
    private String imagePath;

    @Column(name = "begin_time")
    @ApiModelProperty(value = "直播开始时间")
    private Timestamp beginTime;

    @Column(name = "created_time")
    @CreationTimestamp
    @ApiModelProperty(value = "直播创建日期")
    private Timestamp createdTime;

    public void copy(WorksPodcast source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}