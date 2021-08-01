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
package me.tiger.modules.works.service.impl;

import me.tiger.modules.works.domain.WorksPodcast;
import me.tiger.modules.works.service.mapstruct.WorksPodcastMapper;
import me.tiger.utils.ValidationUtil;
import me.tiger.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import me.tiger.modules.works.repository.WorksPodcastRepository;
import me.tiger.modules.works.service.WorksPodcastService;
import me.tiger.modules.works.service.dto.WorksPodcastDto;
import me.tiger.modules.works.service.dto.WorksPodcastQueryCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import me.tiger.utils.PageUtil;
import me.tiger.utils.QueryHelp;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
* @website https://el-admin.vip
* @description 服务实现
* @author tiger
* @date 2021-07-31
**/
@Service
@RequiredArgsConstructor
public class WorksPodcastServiceImpl implements WorksPodcastService {

    private final WorksPodcastRepository worksPodcastRepository;
    private final WorksPodcastMapper worksPodcastMapper;

    @Override
    public Map<String,Object> queryAll(WorksPodcastQueryCriteria criteria, Pageable pageable){
        Page<WorksPodcast> page = worksPodcastRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(worksPodcastMapper::toDto));
    }

    @Override
    public List<WorksPodcastDto> queryAll(WorksPodcastQueryCriteria criteria){
        return worksPodcastMapper.toDto(worksPodcastRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    @Override
    public List<WorksPodcastDto> queryAll() {
        return worksPodcastMapper.toDto(worksPodcastRepository.findAllPodcast());
    }

    @Override
    @Transactional
    public WorksPodcastDto findById(Integer id) {
        WorksPodcast worksPodcast = worksPodcastRepository.findById(id).orElseGet(WorksPodcast::new);
        ValidationUtil.isNull(worksPodcast.getId(),"WorksPodcast","id",id);
        return worksPodcastMapper.toDto(worksPodcast);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorksPodcastDto create(WorksPodcast resources) {
        return worksPodcastMapper.toDto(worksPodcastRepository.save(resources));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WorksPodcast resources) {
        WorksPodcast worksPodcast = worksPodcastRepository.findById(resources.getId()).orElseGet(WorksPodcast::new);
        ValidationUtil.isNull( worksPodcast.getId(),"WorksPodcast","id",resources.getId());
        worksPodcast.copy(resources);
        worksPodcastRepository.save(worksPodcast);
    }

    @Override
    public void deleteAll(Integer[] ids) {
        for (Integer id : ids) {
            worksPodcastRepository.deleteById(id);
        }
    }

    @Override
    public void download(List<WorksPodcastDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (WorksPodcastDto worksPodcast : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("直播链接地址", worksPodcast.getUrl());
            map.put("海报相对路径", worksPodcast.getImagePath());
            map.put("直播开始时间", worksPodcast.getBeginTime());
            map.put("直播创建日期", worksPodcast.getCreatedTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }


}