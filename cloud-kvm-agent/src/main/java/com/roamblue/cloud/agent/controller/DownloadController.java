package com.roamblue.cloud.agent.controller;

import com.roamblue.cloud.agent.service.DownloadService;
import com.roamblue.cloud.common.bean.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "模版文件下载")
@Slf4j
public class DownloadController {
    @Autowired
    private DownloadService downloadService;


    @PostMapping("/download/template")
    @ApiOperation(value = "下载模版")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "任务ID")
    })
    public ResultUtil<Long> downloadTemplate(@RequestParam("uri") String uri, @RequestParam("path") String path) {
        return downloadService.downloadTemplate(uri, path);
    }
}
