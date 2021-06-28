package cn.roamblue.cloud.agent.controller;

import cn.roamblue.cloud.agent.service.DownloadService;
import cn.roamblue.cloud.common.bean.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenjun
 */
@RestController
@Slf4j
public class DownloadController {
    @Autowired
    private DownloadService downloadService;

    /**
     * 下载模版
     *
     * @param uri
     * @param path
     * @return
     */
    public ResultUtil<Long> downloadTemplate(@RequestParam("uri") String uri, @RequestParam("path") String path) {
        return downloadService.downloadTemplate(uri, path);
    }
}
