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
package me.tiger.modules.works.repository;

import me.tiger.modules.works.domain.WorksPodcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
* @website https://el-admin.vip
* @author tiger
* @date 2021-07-31
**/
public interface WorksPodcastRepository extends JpaRepository<WorksPodcast, Integer>, JpaSpecificationExecutor<WorksPodcast> {

    @Query(nativeQuery = true, value = "select * from works_podcast order by created_time desc")
    List<WorksPodcast> findAllPodcast();
}