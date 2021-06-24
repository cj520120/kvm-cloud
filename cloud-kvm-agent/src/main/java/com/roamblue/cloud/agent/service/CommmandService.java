package com.roamblue.cloud.agent.service;

import com.roamblue.cloud.common.bean.ResultUtil;

import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
public interface CommmandService {

    /**
     * qma执行
     *
     * @param name
     * @param command
     * @param timeout
     * @return
     */
    ResultUtil<Map<String, Object>> execute(String name, String command, int timeout);

    /**
     * 写入文件
     *
     * @param name
     * @param path
     * @param body
     * @return
     */
    ResultUtil<Void> writeFile(String name, String path, String body);

    /**
     * qma执行
     *
     * @param name
     * @param commandStr
     * @param args
     * @param timeout
     * @return
     */
    ResultUtil<Map<String, Object>> execute(String name, String commandStr, List<String> args, int timeout);
}
