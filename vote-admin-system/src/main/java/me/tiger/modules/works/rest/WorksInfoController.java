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
package me.tiger.modules.works.rest;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.tiger.annotation.Log;
import me.tiger.modules.works.constant.LifeStatus;
import me.tiger.modules.works.constant.ResourceConstant;
import me.tiger.modules.works.constant.ResponseConstant;
import me.tiger.modules.works.constant.Type;
import me.tiger.modules.works.domain.WorksInfo;
import me.tiger.modules.works.rest.dto.WorksInfoReqDto;
import me.tiger.modules.works.service.WorksInfoService;
import me.tiger.modules.works.service.dto.VoteDto;
import me.tiger.modules.works.service.dto.WorksInfoDto;
import me.tiger.modules.works.service.dto.WorksInfoQueryCriteria;
import me.tiger.utils.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author tiger
 * @website https://el-admin.vip
 * @date 2021-07-24
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "作品信息管理")
@RequestMapping("/api/worksInfo")
public class WorksInfoController {

    private final WorksInfoService worksInfoService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('worksInfo:list')")
    public void download(HttpServletResponse response, WorksInfoQueryCriteria criteria) throws IOException {
        worksInfoService.download(worksInfoService.queryAll(criteria), response);
    }

    @PostMapping(value = "/vote")
    @Log("投票")
    @ApiOperation("投票")
//    @PreAuthorize("@el.check('worksInfo:add')")
    public ResponseEntity<Object> voteWorksInfo(@RequestHeader("openId") String wxOpenId, @RequestBody VoteDto voteDto) {

        try {
            worksInfoService.voteWorksInfo(voteDto, wxOpenId);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException", e);
            return new ResponseEntity<>(ResponseConstant.buildResult(ResponseConstant.FAIL, e.getMessage(), null), HttpStatus.OK);
        } catch (Exception e) {
            log.error("系统异常", e);
            return new ResponseEntity<>(ResponseConstant.buildResult(ResponseConstant.FAIL, e.getMessage(), null), HttpStatus.OK);
        }

        return new ResponseEntity<>(ResponseConstant.buildResult(ResponseConstant.SUCCESS, "投票成功", null), HttpStatus.OK);
    }

    @GetMapping
    @Log("查询作品信息")
    @ApiOperation("查询作品信息")
//    @PreAuthorize("@el.check('worksInfo:list')")
    public ResponseEntity<Object> query(@RequestHeader(value = "openId", required = false) String openId, WorksInfoQueryCriteria criteria, Pageable pageable) {

        if (StringUtils.isEmpty(openId)) {
            return new ResponseEntity<>(worksInfoService.queryAll(criteria, pageable), HttpStatus.OK);
        } else {
            Map<String, Object> worksInfo = worksInfoService.findWorksInfo(criteria, pageable);
            return new ResponseEntity<>(ResponseConstant.buildResult(ResponseConstant.SUCCESS, "请求成功", worksInfo), HttpStatus.OK);
        }

    }

    @GetMapping(value = "/list")
    @Log("查询获奖或者人气作品信息")
    @ApiOperation("查询获奖或者人气作品信息")
//    @PreAuthorize("@el.check('worksInfo:list')")
    public ResponseEntity<Object> findWorksInfoWithWinFlag(@RequestParam("winFlag") Integer winFlag, Pageable pageable) {

        List<WorksInfoDto> worksInfo = worksInfoService.findWorksInfo(winFlag, pageable);

        return new ResponseEntity<>(ResponseConstant.buildResult(ResponseConstant.SUCCESS, "请求成功", worksInfo), HttpStatus.OK);
    }

    @PostMapping
    @Log("新增作品信息")
    @ApiOperation("新增作品信息")
    @PreAuthorize("@el.check('worksInfo:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody WorksInfo resources) {
        return new ResponseEntity<>(worksInfoService.create(resources), HttpStatus.CREATED);
    }

    @Log("创建文章类参赛作品")
    @ApiOperation("创建文章类参赛作品")
//    @PreAuthorize("@el.check('worksInfo:add')")
    @PostMapping("/article")
    public ResponseEntity<JSONObject> createParagraph(@RequestHeader("openId") String openId,
                                                      @RequestBody WorksInfoReqDto dto) {

        try {
            WorksInfo worksInfo = WorksInfo.builder()
                    .type(Type.ARTICLE.getCode()).wxOpenId(openId)
                    .authorName(dto.getUserName()).authorMobile(dto.getPhone())
                    .selfDescription(dto.getDescription()).lifeStatus(LifeStatus.SUBMIT.getCode()).build();
            worksInfoService.saveArticle(worksInfo, dto.getArticle());
            return new ResponseEntity<>(ResponseConstant.buildResult(ResponseConstant.SUCCESS, "保存成功", null), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(ResponseConstant.buildResult(ResponseConstant.FAIL, e.getMessage(), null), HttpStatus.CREATED);
        }
    }

    @Log("创建图片类参赛作品")
    @ApiOperation("创建图片类参赛作品")
//    @PreAuthorize("@el.check('worksInfo:add')")
    @PostMapping("/images")
    public ResponseEntity<JSONObject> createImagesWork(@RequestHeader("openId") String openId,
                                                       @RequestParam("userName") String userName,
                                                       @RequestParam("phone") String phone,
                                                       @RequestParam("description") String description,
                                                       @RequestParam("images") MultipartFile[] images) {

        if (images.length == 0) {
            return new ResponseEntity<>(ResponseConstant.buildResult(ResponseConstant.FAIL, "图片列表不能为空，请上传作品有关图片", null), HttpStatus.CREATED);
        }

        try {
            WorksInfo worksInfo = WorksInfo.builder()
                    .type(Type.IMAGES.getCode()).wxOpenId(openId)
                    .authorName(userName).authorMobile(phone)
                    .selfDescription(description).lifeStatus(LifeStatus.SUBMIT.getCode()).build();

            //如果图片上传发生异常，直接返回报错,用户可以重试上传
            // 如果一直失败就需要系统管理员介入
            List<String> pathList = new ArrayList<>(images.length);
            for (MultipartFile imageFile : images) {
                try {
                    String relativeFilePath = String.format("%d_%s", System.currentTimeMillis(), imageFile.getOriginalFilename());
                    Path path = ResourceConstant.FILE_ROOT.resolve(relativeFilePath);
                    imageFile.transferTo(path);
                    pathList.add(ResourceConstant.getFileRelativeUrl(relativeFilePath));
                } catch (IOException e) {
                    return new ResponseEntity<>(ResponseConstant.buildResult(0, e.getMessage(), null), HttpStatus.CREATED);
                }
            }

            worksInfoService.saveWorksInfoWithFiles(worksInfo, pathList);
        } catch (Exception e) {
            return new ResponseEntity<>(ResponseConstant.buildResult(ResponseConstant.FAIL, e.getMessage(), null), HttpStatus.CREATED);
        }

        return new ResponseEntity<>(ResponseConstant.buildResult(ResponseConstant.SUCCESS, "保存成功", null), HttpStatus.CREATED);
    }

    @PostMapping(value = "/video", headers = "content-type=multipart/form-data")
    @Log("创建视频类参赛作品")
    @ApiOperation("为作品上传视频")
//    @PreAuthorize("@el.check('worksInfo:add')")
    public ResponseEntity<JSONObject> createVideoWorks(@RequestHeader("openId") String openId, @RequestParam("userName") String userName,
                                                       @RequestParam("phone") String phone,
                                                       @RequestParam("description") String description,
                                                       @RequestParam("video") MultipartFile video) {

        if (video.isEmpty()) {
            JSONObject jsonObject = ResponseConstant.buildResult(0, "文件为空,请选择你的文件上传", null);
            return new ResponseEntity<>(jsonObject, HttpStatus.NO_CONTENT);
        }

        String relativeFilePath = String.format("%d_%s", System.currentTimeMillis(), video.getOriginalFilename());
        try {
            WorksInfo worksInfo = WorksInfo.builder()
                    .type(Type.VIDEO.getCode()).wxOpenId(openId)
                    .authorName(userName).authorMobile(phone)
                    .selfDescription(description).lifeStatus(LifeStatus.SUBMIT.getCode()).build();
            Path path = ResourceConstant.FILE_ROOT.resolve(relativeFilePath);
            video.transferTo(path);

            worksInfoService.saveWorksInfoWithFiles(worksInfo, Arrays.asList(ResourceConstant.getFileRelativeUrl(relativeFilePath)));
            return new ResponseEntity<>(ResponseConstant.buildResult(ResponseConstant.SUCCESS, "保存成功", null), HttpStatus.CREATED);
        } catch (IOException e) {
            log.info("上传视频发生错误", e);
            return new ResponseEntity<>(ResponseConstant.buildResult(ResponseConstant.FAIL, e.getMessage(), null), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }


    @PutMapping
    @Log("修改作品信息")
    @ApiOperation("修改作品信息")
    @PreAuthorize("@el.check('worksInfo:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody WorksInfo resources) {
        worksInfoService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除作品信息")
    @ApiOperation("删除作品信息")
    @PreAuthorize("@el.check('worksInfo:del')")
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody Integer[] ids) {
        worksInfoService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}