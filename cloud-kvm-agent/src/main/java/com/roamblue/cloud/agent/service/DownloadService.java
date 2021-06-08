package com.roamblue.cloud.agent.service;

import com.roamblue.cloud.common.bean.ResultUtil;

public interface DownloadService {
    /**
     * 下载文件
     *
     * @param uri
     * @param path
     * @return
     */
    ResultUtil<Long> downloadTemplate(String uri, String path);
}
