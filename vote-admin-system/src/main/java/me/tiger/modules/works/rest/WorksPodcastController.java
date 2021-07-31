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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.tiger.annotation.Log;
import me.tiger.modules.works.constant.ResponseConstant;
import me.tiger.modules.works.domain.WorksPodcast;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author tiger
 * @website https://el-admin.vip
 * @date 2021-07-31
 **/
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