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
import com.sun.jna.platform.win32.Sspi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.tiger.annotation.Log;
import me.tiger.modules.works.constant.ResourceConstant;
import me.tiger.modules.works.constant.ResponseConstant;
import me.tiger.modules.works.domain.WorksPodcast;
import me.tiger.modules.works.rest.dto.WorksPodcastReqDto;
import me.tiger.modules.works.service.WorksPodcastService;
import me.tiger.modules.works.service.dto.WorksPodcastDto;
import me.tiger.modules.works.service.dto.WorksPodcastQueryCriteria;
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
import java.sql.Timestamp;
import java.util.List;

/**
 * @author tiger
 * @website https://el-admin.vip
 * @date 2021-07-31
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "作品有关直播管理")
@RequestMapping("/api/worksPodcast")
public class WorksPodcastController {

    private final WorksPodcastService worksPodcastService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('worksPodcast:list')")
    public void download(HttpServletResponse response, WorksPodcastQueryCriteria criteria) throws IOException {
        worksPodcastService.download(worksPodcastService.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询作品有关直播")
    @ApiOperation("查询作品有关直播")
//    @PreAuthorize("@el.check('worksPodcast:list')")
    public ResponseEntity<Object> query(@RequestHeader(value = "openId", required = false) String openId, WorksPodcastQueryCriteria criteria, Pageable pageable) {
        if (StringUtils.isEmpty(openId)) {
            return new ResponseEntity<>(worksPodcastService.queryAll(criteria, pageable), HttpStatus.OK);
        } else {
            List<WorksPodcastDto> worksPodcastDtos = worksPodcastService.queryAll();
            return new ResponseEntity<>(ResponseConstant.buildResult(ResponseConstant.SUCCESS, "查询成功", worksPodcastDtos), HttpStatus.OK);
        }
    }

    @PostMapping(value = "uploadBanner")
    @Log("新增作品有关直播")
    @ApiOperation("新增作品有关直播")
    @PreAuthorize("@el.check('worksPodcast:add')")
    public ResponseEntity<Object> create(@RequestParam(value = "file") MultipartFile bannerImage) {

        if (bannerImage.isEmpty()) {
            return new ResponseEntity<>("图片为空,请选择你的文件上传", HttpStatus.NOT_ACCEPTABLE);
        }

        String relativeFilePath = String.format("%d_%s", System.currentTimeMillis(), bannerImage.getOriginalFilename());
        Path path = ResourceConstant.FILE_ROOT.resolve(relativeFilePath);
        try {
            bannerImage.transferTo(path);
        } catch (IOException e) {
            log.error("文件上传失败", e);
        }

        return new ResponseEntity<>(ResourceConstant.getFileRelativeUrl(relativeFilePath), HttpStatus.CREATED);
    }


    @PostMapping
    @Log("新增作品有关直播")
    @ApiOperation("新增作品有关直播")
    @PreAuthorize("@el.check('worksPodcast:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody WorksPodcast resources) {
        return new ResponseEntity<>(worksPodcastService.create(resources), HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改作品有关直播")
    @ApiOperation("修改作品有关直播")
    @PreAuthorize("@el.check('worksPodcast:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody WorksPodcast resources) {
        worksPodcastService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除作品有关直播")
    @ApiOperation("删除作品有关直播")
    @PreAuthorize("@el.check('worksPodcast:del')")
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody Integer[] ids) {
        worksPodcastService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}