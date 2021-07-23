package cn.roamblue.cloud.agent.service.impl;

import cn.hutool.http.HttpUtil;
import cn.roamblue.cloud.agent.service.DownloadService;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class DownloadServiceImpl implements DownloadService {
    @Override
    public ResultUtil<Long> downloadTemplate(String uri, String path) {
        log.info("开始下载模版.uri={},path={}",uri,path);
        File file = new File(path);
        try {
            HttpUtil.downloadFile(uri, file);
            return ResultUtil.<Long>builder().data(file.length()).build();
        } catch (Exception err) {
            log.error("下载模版文件出错.uri={}.file={}",uri,path,err);
            if (file.exists()) {
                if (file.delete()) {
                    //do nothing
                }
            }
            return ResultUtil.<Long>builder().code(ErrorCode.SERVER_ERROR).build();
        }
    }
}
