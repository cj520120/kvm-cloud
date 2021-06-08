package com.roamblue.cloud.agent.service;

import com.roamblue.cloud.common.bean.ResultUtil;

import java.util.List;
import java.util.Map;

public interface CommmandService {


    ResultUtil<Map<String, Object>> execute(String name, String command, int timeout);

    ResultUtil<Void> writeFile(String name, String path, String body);

    ResultUtil<Map<String, Object>> execute(String name, String commandStr, List<String> args, int timeout);
}
