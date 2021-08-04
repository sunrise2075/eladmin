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

import lombok.Data;
import java.sql.Timestamp;
import java.util.List;
import me.tiger.annotation.Query;

/**
* @website https://el-admin.vip
* @author tiger
* @date 2021-07-31
**/
@Data
public class WorksPodcastQueryCriteria{

    /** 模糊 */
    @Query(type = Query.Type.INNER_LIKE)
    private String url;

    /** 精确 */
    @Query
    private String imagePath;
    /** BETWEEN */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> beginTime;
}