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

import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import me.tiger.modules.works.constant.Type;
import me.tiger.modules.works.domain.*;
import me.tiger.modules.works.repository.*;
import me.tiger.modules.works.service.WorksInfoService;
import me.tiger.modules.works.service.WorksPodcastService;
import me.tiger.modules.works.service.dto.*;
import me.tiger.modules.works.service.mapstruct.WorksInfoMapper;
import me.tiger.modules.works.service.mapstruct.WorksPodcastMapper;
import me.tiger.utils.FileUtil;
import me.tiger.utils.PageUtil;
import me.tiger.utils.QueryHelp;
import me.tiger.utils.ValidationUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tiger
 * @website https://el-admin.vip
 * @description 服务实现
 * @date 2021-07-24
 **/
@Service
@RequiredArgsConstructor
public class WorksInfoServiceImpl implements WorksInfoService {

    private final WorksInfoRepository worksInfoRepository;
    private final WorksInfoMapper worksInfoMapper;
    private final WorksArticleRepository worksArticleRepository;
    private final WorksFilesRepository worksFilesRepository;
    private final WorksVoteRecordRepository worksVoteRecordRepository;
    private final WxWorksAuthorRepository wxWorksAuthorRepository;

    @Value("${vote.limit:10}")
    private Integer voteLimit;

    @Override
    public Map<String, Object> queryAll(WorksInfoQueryCriteria criteria, Pageable pageable) {
        WorksInfo worksInfo = WorksInfo.builder().authorName(criteria.getAuthorName()).authorMobile(criteria.getAuthorMobile()).type(criteria.getType()).build();
        Page<WorksInfo> page = worksInfoRepository.findAll(Example.of(worksInfo), pageable);
        return PageUtil.toPage(page.map(worksInfoMapper::toDto));
    }

    @Override
    public List<WorksInfoDto> queryAll(WorksInfoQueryCriteria criteria) {
        return worksInfoMapper.toDto(worksInfoRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    @Transactional
    public WorksInfoDto findById(Integer id) {
        WorksInfo worksInfo = worksInfoRepository.findById(id).orElseGet(WorksInfo::new);
        ValidationUtil.isNull(worksInfo.getId(), "WorksInfo", "id", id);
        return worksInfoMapper.toDto(worksInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorksInfoDto create(WorksInfo resources) {
        return worksInfoMapper.toDto(worksInfoRepository.save(resources));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WorksInfo resources) {
        WorksInfo worksInfo = worksInfoRepository.findById(resources.getId()).orElseGet(WorksInfo::new);
        ValidationUtil.isNull(worksInfo.getId(), "WorksInfo", "id", resources.getId());
        worksInfo.copy(resources);
        worksInfoRepository.save(worksInfo);
    }

    @Override
    public void deleteAll(Integer[] ids) {
        for (Integer id : ids) {
            worksInfoRepository.deleteById(id);
        }
    }

    @Override
    public void download(List<WorksInfoDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (WorksInfoDto worksInfo : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("作者姓名", worksInfo.getAuthorName());
            map.put("作者手机号", worksInfo.getAuthorMobile());
            map.put("作品描述", worksInfo.getSelfDescription());
            map.put("作品种类: 0. 文字类  1. 图片类  2. 视频类", worksInfo.getType());
            map.put("作品状态：1. 提交  2. 审核中  3. 审核通过  4. 审核拒绝 5. 获奖", worksInfo.getLifeStatus());
            map.put("创建日期", worksInfo.getCreatedDate());
            map.put("创建人", worksInfo.getCreatedBy());
            map.put("更新人", worksInfo.getUpdatedBy());
            map.put("更新日期", worksInfo.getUpdatedDate());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public void saveArticle(WorksInfo worksInfo, String article) {
        //1。 保存文字类作品
        WorksInfo savedWorks = worksInfoRepository.save(worksInfo);

        if (Type.ARTICLE.getCode().equals(savedWorks.getType())) {
            WorksArticle worksArticle = WorksArticle.builder().worksId(savedWorks.getId()).articleContent(article).build();
            worksArticleRepository.save(worksArticle);
        }
    }

    @Override
    public void saveWorksInfoWithFiles(WorksInfo worksInfo, List<String> pathList) {
        //2. 保存图片类作品
        WorksInfo savedWorks = worksInfoRepository.save(worksInfo);

        if (Type.IMAGES.getCode().equals(savedWorks.getType()) || Type.VIDEO.getCode().equals(savedWorks.getType())) {

            List<WorksFiles> worksFilesList = pathList.stream().map(s -> {
                return WorksFiles.builder().worksId(savedWorks.getId()).relativeFilePath(s).build();
            }).collect(Collectors.toList());

            worksFilesRepository.saveAll(worksFilesList);
        }
    }

    @Override
    public Map<String, Object> findWorksInfo(WorksInfoQueryCriteria criteria, Pageable pageable) {

        Page<WorksInfo> worksInfoList = worksInfoRepository.findWorksInfo(criteria.getAuthorName(), criteria.getAuthorMobile(), criteria.getType(), pageable);

        if (!CollectionUtils.isEmpty(worksInfoList.getContent())) {

            return transformWorksInfoDto(pageable, worksInfoList);
        } else {

            return PageUtil.toPage(0, 0, 0, java.util.Collections.emptyList());
        }
    }

    private Map<String, String> getHeadImgUrlMapping(List<String> wxIds) {
        Map<String, String> headImgs;
        if (CollectionUtils.isEmpty(wxIds)) {
            headImgs = java.util.Collections.emptyMap();
        } else {
            headImgs = wxWorksAuthorRepository.findAuthors(wxIds).stream().collect(Collectors.toMap(WxWorksAuthor::getOpenId, WxWorksAuthor::getHeadImgUrl));
        }
        return headImgs;
    }

    @Override
    @Transactional
    public void voteWorksInfo(VoteDto voteDto) throws IllegalAccessException {
        Optional<WorksInfo> optionalWorksInfo = worksInfoRepository.findById(voteDto.getWorksId());
        if (optionalWorksInfo.isPresent()) {

            //检查本用户当天投票次数是否超过要求
            checkVoteCount(voteDto);

            //保存投票记录
            WorksVoteRecord worksVoteRecord = WorksVoteRecord.builder().worksId(voteDto.getWorksId()).count(voteDto.getCount()).voterUserName(voteDto.getVoterUserName()).build();
            worksVoteRecordRepository.save(worksVoteRecord);

            WorksInfo worksInfo = optionalWorksInfo.get();
            //更新总票数
            Integer voteCount = worksInfo.getVoteCount();
            voteCount = voteCount + voteDto.getCount();
            worksInfo.setVoteCount(voteCount);
            worksInfoRepository.save(worksInfo);
        }
    }

    @Override
    public Map<String, Object> findWorksInfo(Integer winFlag, Pageable pageable) {

        Page<WorksInfo> worksInfos = worksInfoRepository.findWorksInfo(winFlag, pageable);

        if (!CollectionUtils.isEmpty(worksInfos.getContent())) {

            return transformWorksInfoDto(pageable, worksInfos);

        } else {
            return PageUtil.toPage(0, 0, 0, java.util.Collections.emptyList());
        }
    }

    private Map<String, Object> transformWorksInfoDto(Pageable pageable, Page<WorksInfo> worksInfos) {

        List<String> wxIds = worksInfos.stream().map(WorksInfo::getWxOpenId).collect(Collectors.toList());

        Map<String, String> headImgs = getHeadImgUrlMapping(wxIds);

        List<WorksInfoDto> worksInfoDtos = worksInfos.getContent().stream().map(worksInfo -> {

            WorksInfoDto dto = new WorksInfoDto();
            BeanUtils.copyProperties(worksInfo, dto);
            dto.setHeadImgUrl(headImgs.getOrDefault(worksInfo.getWxOpenId(), ""));

            //文字类
            if (worksInfo.getType() == 0) {
                List<WorksArticle> worksArticleList = worksArticleRepository.findAll(Example.of(WorksArticle.builder().worksId(worksInfo.getId()).build()));
                if (!Collections.isEmpty(worksArticleList)) {
                    dto.setArticleContent(worksArticleList.get(0).getArticleContent());
                }
            } else {
                //图片或者视频类
                List<WorksFiles> worksFilesList = worksFilesRepository.findAll(Example.of(WorksFiles.builder().worksId(worksInfo.getId()).build()));
                List<String> filePathList = worksFilesList.stream().map(WorksFiles::getRelativeFilePath).collect(Collectors.toList());
                dto.setFiles(filePathList);
            }

            return dto;
        }).collect(Collectors.toList());

        Long totalElements = worksInfos.getTotalElements();

        return PageUtil.toPage(totalElements.intValue(), pageable.getPageSize(), pageable.getPageNumber(), worksInfoDtos);
    }

    private void checkVoteCount(VoteDto voteDto) throws IllegalAccessException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 23, 59, 59, 999999999);
        Integer count = worksVoteRecordRepository.countVote(voteDto.getVoterUserName(), Timestamp.valueOf(start), Timestamp.valueOf(end));
        if (count > voteLimit) {
            throw new IllegalAccessException(String.format("每天投票不能超过%s票", voteLimit));
        }
    }

    /**
    * @website https://el-admin.vip
    * @description 服务实现
    * @author tiger
    * @date 2021-07-31
    **/
    @Service
    @RequiredArgsConstructor
    public static class WorksPodcastServiceImpl implements WorksPodcastService {

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
}