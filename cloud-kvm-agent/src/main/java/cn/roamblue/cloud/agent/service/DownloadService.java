package cn.roamblue.cloud.agent.service;

import cn.roamblue.cloud.common.bean.ResultUtil;

/**
 * @author chenjun
 */
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
