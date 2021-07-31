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
import lombok.extern.slf4j.Slf4j;
import me.tiger.modules.works.constant.Type;
import me.tiger.modules.works.domain.*;
import me.tiger.modules.works.repository.*;
import me.tiger.modules.works.service.WorksInfoService;
import me.tiger.modules.works.service.dto.VoteDto;
import me.tiger.modules.works.service.dto.WorksInfoDto;
import me.tiger.modules.works.service.dto.WorksInfoQueryCriteria;
import me.tiger.modules.works.service.mapstruct.WorksInfoMapper;
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
@Slf4j
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

    private Map<String, WxWorksAuthor> getHeadImgUrlMapping(List<String> wxIds) {
        Map<String, WxWorksAuthor> headImgs;
        if (CollectionUtils.isEmpty(wxIds)) {
            headImgs = java.util.Collections.emptyMap();
        } else {
            headImgs = wxWorksAuthorRepository.findAuthors(wxIds).stream().collect(Collectors.toMap(WxWorksAuthor::getOpenId, wxWorksAuthor -> wxWorksAuthor));
        }
        return headImgs;
    }

    @Override
    @Transactional
    public void voteWorksInfo(VoteDto voteDto, String wxOpenId) throws IllegalAccessException {
        Optional<WorksInfo> optionalWorksInfo = worksInfoRepository.findById(voteDto.getWorksId());
        if (optionalWorksInfo.isPresent()) {

            //检查本用户当天投票次数是否超过要求
            checkVoteCount(wxOpenId);

            //保存投票记录
            WorksVoteRecord worksVoteRecord = WorksVoteRecord.builder().worksId(voteDto.getWorksId()).count(voteDto.getCount()).voterUserName(wxOpenId).createdTime(new Timestamp(new Date().getTime())).build();
            worksVoteRecordRepository.save(worksVoteRecord);

            WorksInfo worksInfo = optionalWorksInfo.get();
            //更新总票数
            Integer voteCount = worksInfo.getVoteCount();
            if (voteCount == null) {
                voteCount = 0;
            }
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

        Map<String, WxWorksAuthor> wxWorksAuthorMap = getHeadImgUrlMapping(wxIds);

        List<WorksInfoDto> worksInfoDtos = worksInfos.getContent().stream().map(worksInfo -> {

            WorksInfoDto dto = new WorksInfoDto();
            BeanUtils.copyProperties(worksInfo, dto);
            if (wxWorksAuthorMap.containsKey(worksInfo.getWxOpenId())) {
                WxWorksAuthor wxWorksAuthor = wxWorksAuthorMap.get(worksInfo.getWxOpenId());
                dto.setAuthorName(wxWorksAuthor.getNickName());
                dto.setHeadImgUrl(wxWorksAuthor.getHeadImgUrl());
            }

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

    private void checkVoteCount(String wxOpenId) throws IllegalAccessException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 23, 59, 59, 999999999);
        Integer count = worksVoteRecordRepository.countVote(wxOpenId, Timestamp.valueOf(start), Timestamp.valueOf(end));

        if (count == null) {
            count = 0;
        }
        log.info("当前用户今天已经投票的数量是:{}", count);
        if (count > voteLimit) {
            throw new IllegalAccessException(String.format("每天投票不能超过%s票", voteLimit));
        }
    }
}