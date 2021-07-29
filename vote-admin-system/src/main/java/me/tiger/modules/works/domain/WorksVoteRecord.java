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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

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
@Table(name="works_vote_record")
public class WorksVoteRecord implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ApiModelProperty(value = "自增主键")
    private Integer id;

    @Column(name = "works_id")
    @ApiModelProperty(value = "作品id")
    private Integer worksId;

    @Column(name = "count")
    @ApiModelProperty(value = "本次投票点击次数")
    private Integer count;

    @Column(name = "voter_user_name")
    @ApiModelProperty(value = "投票人用户名")
    private String voterUserName;

    @Column(name = "created_time")
    @ApiModelProperty(value = "投票时间")
    @CreatedDate
    private Timestamp createdTime;

    public void copy(WorksVoteRecord source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}