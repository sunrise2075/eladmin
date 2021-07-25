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
import me.tiger.annotation.Log;
import me.tiger.modules.works.constant.LifeStatus;
import me.tiger.modules.works.constant.Type;
import me.tiger.modules.works.domain.WorksInfo;
import me.tiger.modules.works.service.WorksInfoService;
import me.tiger.modules.works.service.dto.WorksInfoQueryCriteria;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author tiger
 * @website https://el-admin.vip
 * @date 2021-07-24
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "作品信息管理")
@RequestMapping("/api/worksInfo")
public class WorksInfoController {

    private static final Path FILE_ROOT = Paths.get("uploads");

    private final WorksInfoService worksInfoService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('worksInfo:list')")
    public void download(HttpServletResponse response, WorksInfoQueryCriteria criteria) throws IOException {
        worksInfoService.download(worksInfoService.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询作品信息")
    @ApiOperation("查询作品信息")
    @PreAuthorize("@el.check('worksInfo:list')")
    public ResponseEntity<Object> query(WorksInfoQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(worksInfoService.queryAll(criteria, pageable), HttpStatus.OK);
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
    @PreAuthorize("@el.check('worksInfo:add')")
    @PostMapping("/article")
    public ResponseEntity<JSONObject> createParagraph(@RequestParam("username") String userName,
                                                      @RequestParam("phone") String phone,
                                                      @RequestParam("description") String description,
                                                      @RequestParam("article") String article) {

        try {
            WorksInfo worksInfo = WorksInfo.builder()
                    .type(Type.ARTICLE.getCode())
                    .authorName(userName).authorMobile(phone)
                    .selfDescription(description).lifeStatus(LifeStatus.SUBMIT.getCode()).build();
            worksInfoService.saveArticle(worksInfo, article);
            return new ResponseEntity<>(buildResult(1, "保存成功", null), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(buildResult(0, e.getMessage(), null), HttpStatus.CREATED);
        }
    }

    @Log("创建图片类参赛作品")
    @ApiOperation("创建图片类参赛作品")
    @PreAuthorize("@el.check('worksInfo:add')")
    @PostMapping("/images")
    public ResponseEntity<JSONObject> createImagesWork(@RequestParam("username") String userName,
                                                       @RequestParam("phone") String phone,
                                                       @RequestParam("description") String description,
                                                       @RequestParam("images") MultipartFile[] images) {

        if (images.length == 0) {
            return new ResponseEntity<>(buildResult(0, "图片列表不能为空，请上传作品有关图片", null), HttpStatus.CREATED);
        }

        try {
            WorksInfo worksInfo = WorksInfo.builder()
                    .type(Type.IMAGES.getCode())
                    .authorName(userName).authorMobile(phone)
                    .selfDescription(description).lifeStatus(LifeStatus.SUBMIT.getCode()).build();

            //如果图片上传发生异常，直接返回报错,用户可以重试上传
            // 如果一直失败就需要系统管理员介入
            List<String> pathList = new ArrayList<>(images.length);
            for (MultipartFile imageFile : images) {
                try {
                    String relativeFilePath = String.format("%d_%s", System.currentTimeMillis(), imageFile.getOriginalFilename());
                    Path path = FILE_ROOT.resolve(relativeFilePath);
                    imageFile.transferTo(path);
                    pathList.add(relativeFilePath);
                } catch (IOException e) {
                    return new ResponseEntity<>(buildResult(0, e.getMessage(), null), HttpStatus.CREATED);
                }
            }

            worksInfoService.saveWorksInfoWithFiles(worksInfo, pathList);
        } catch (Exception e) {
            return new ResponseEntity<>(buildResult(0, e.getMessage(), null), HttpStatus.CREATED);
        }

        return new ResponseEntity<>(buildResult(1, "保存成功", null), HttpStatus.CREATED);
    }

    @PostMapping(value = "/video", headers = "content-type=multipart/form-data")
    @Log("创建视频类参赛作品")
    @ApiOperation("为作品上传视频")
    @PreAuthorize("@el.check('worksInfo:add')")
    public ResponseEntity<JSONObject> createVideoWorks(@RequestParam("username") String userName,
                                                       @RequestParam("phone") String phone,
                                                       @RequestParam("description") String description,
                                                       @RequestParam("video") MultipartFile video) {

        if (video.isEmpty()) {
            JSONObject jsonObject = buildResult(0, "文件为空,请选择你的文件上传", null);
            return new ResponseEntity<>(jsonObject, HttpStatus.NO_CONTENT);
        }

        String relativeFilePath = String.format("%d_%s", System.currentTimeMillis(), video.getOriginalFilename());
        try {
            WorksInfo worksInfo = WorksInfo.builder()
                    .type(Type.IMAGES.getCode())
                    .authorName(userName).authorMobile(phone)
                    .selfDescription(description).lifeStatus(LifeStatus.SUBMIT.getCode()).build();
            Path path = FILE_ROOT.resolve(relativeFilePath);
            video.transferTo(path);
            JSONObject data = new JSONObject();
            data.put("file", relativeFilePath);
            worksInfoService.saveWorksInfoWithFiles(worksInfo, Arrays.asList(relativeFilePath));
            return new ResponseEntity<>(buildResult(0, "保存成功", null), HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (IOException e) {

            return new ResponseEntity<>(buildResult(0, e.getMessage(), null), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private JSONObject buildResult(int code, String message, Object data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", message);
        jsonObject.put("data", data);
        return jsonObject;
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