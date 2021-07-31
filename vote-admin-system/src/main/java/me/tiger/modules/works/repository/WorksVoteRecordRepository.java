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

import me.tiger.modules.works.domain.WorksVoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;

/**
 * @author tiger
 * @website https://el-admin.vip
 * @date 2021-07-24
 **/
public interface WorksVoteRecordRepository extends JpaRepository<WorksVoteRecord, Integer>, JpaSpecificationExecutor<WorksVoteRecord> {

    @Query(nativeQuery = true, value = "select sum(ifnull(r.count,0))  from works_vote_record r " +
            "where r.created_time between ?2 and ?3 and r.voter_user_name=?1")
    Integer countVote(String voterUserName, Timestamp start, Timestamp end);
}