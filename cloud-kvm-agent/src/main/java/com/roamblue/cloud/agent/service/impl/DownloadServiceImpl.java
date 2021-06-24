package com.roamblue.cloud.agent.service.impl;

import cn.hutool.http.HttpUtil;
import com.roamblue.cloud.agent.service.DownloadService;
import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.common.util.ErrorCode;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author chenjun
 */
@Service
public class DownloadServiceImpl implements DownloadService {
    @Override
    public ResultUtil<Long> downloadTemplate(String uri, String path) {
        File file = new File(path);
        try {
            HttpUtil.downloadFile(uri, file);
            return ResultUtil.<Long>builder().data(file.length()).build();
        } catch (Exception err) {
            if (file.exists()) {
                if (file.delete()) {
                    //do nothing
                }
            }
            return ResultUtil.<Long>builder().code(ErrorCode.SERVER_ERROR).build();
        }
    }
}
