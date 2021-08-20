package cn.roamblue.cloud.agent.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.roamblue.cloud.agent.service.CommmandService;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
@Service
@Slf4j
public class CommmandServiceImpl extends AbstractKvmService implements CommmandService {

    @Override
    public ResultUtil<Map<String, Object>> execute(String name, String command, int timeout) {
        return this.execute(connect -> {
            int[] ids = connect.listDomains();
            for (int id : ids) {
                Domain domain = connect.domainLookupByID(id);
                if (name.equals(domain.getName())) {
                    String response = domain.qemuAgentCommand(command, timeout, 0);
                    if (StringUtils.isEmpty(response)) {
                        throw new CodeException(ErrorCode.VM_NOT_FOUND, "vm not found");
                    }
                    Gson gson = new Gson();
                    Map<String, Object> map = gson.fromJson(response, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    return ResultUtil.<Map<String, Object>>builder().data(map).build();
                }
            }
            throw new CodeException(ErrorCode.VM_NOT_FOUND);
        });
    }

    @Override
    public ResultUtil<Void> writeFile(String name, String path, String body) {
        return this.execute(connect -> {
            int[] ids = connect.listDomains();
            for (int id : ids) {
                Domain domain = connect.domainLookupByID(id);
                if (name.equals(domain.getName())) {
                    Gson gson = new Gson();
                    int handler = openFile(path, domain, gson);
                    wireteFile(body, domain, gson, handler);
                    closeFile(domain, gson, handler);
                    return ResultUtil.<Void>builder().build();
                }
            }
            throw new CodeException(ErrorCode.VM_NOT_FOUND);
        });
    }

    private void closeFile(Domain domain, Gson gson, int handler) throws LibvirtException {
        Map<String, Object> command = new HashMap<>(2);
        command.put("execute", "guest-file-close");
        Map<String, Object> arguments = new HashMap<>(2);
        arguments.put("handle", handler);
        command.put("arguments", arguments);
        String response = domain.qemuAgentCommand(gson.toJson(command), 10, 0);
        if (StringUtils.isEmpty(response)) {
            throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "执行失败");
        }
    }

    private void wireteFile(String body, Domain domain, Gson gson, int handler) throws LibvirtException {
        Map<String, Object> command = new HashMap<>(2);
        command.put("execute", "guest-file-write");
        Map<String, Object> arguments = new HashMap<>(2);
        arguments.put("handle", handler);
        arguments.put("buf-b64", cn.hutool.core.codec.Base64.encode(body.getBytes(StandardCharsets.UTF_8)));
        command.put("arguments", arguments);
        String response = domain.qemuAgentCommand(gson.toJson(command), 10, 0);
        if (StringUtils.isEmpty(response)) {
            throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "执行失败");
        }
    }

    private int openFile(String path, Domain domain, Gson gson) throws LibvirtException {
        int handler;
        Map<String, Object> command = new HashMap<>(2);
        command.put("execute", "guest-file-open");
        Map<String, Object> arguments = new HashMap<>(2);
        arguments.put("path", path);
        arguments.put("mode", "w+");
        command.put("arguments", arguments);
        String response = domain.qemuAgentCommand(gson.toJson(command), 10, 0);
        if (StringUtils.isEmpty(response)) {
            throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "执行失败");
        }
        Map<String, Object> map = gson.fromJson(response, new TypeToken<Map<String, Object>>() {
        }.getType());
        handler = NumberUtil.parseInt(map.get("return").toString());
        return handler;
    }

    @Override
    public ResultUtil<Map<String, Object>> execute(String name, String commandStr, List<String> args, int timeout) {

        return this.execute(connect -> {
            int[] ids = connect.listDomains();
            for (int id : ids) {
                Domain domain = connect.domainLookupByID(id);
                if (name.equals(domain.getName())) {
                    Gson gson = new Gson();
                    Map<String, Object> command = new HashMap<>(2);
                    command.put("execute", "guest-exec");
                    Map<String, Object> arguments = new HashMap<>(3);
                    command.put("arguments", arguments);
                    arguments.put("path", commandStr);
                    arguments.put("arg", args);
                    String request = gson.toJson(command);
                    log.info("excute command={}", request);
                    String response = domain.qemuAgentCommand(request, timeout, 0);
                    if (StringUtils.isEmpty(response)) {
                        throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "执行失败");
                    }
                    Map<String, Object> map = gson.fromJson(response, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    return ResultUtil.<Map<String, Object>>builder().data(map).build();
                }
            }
            throw new CodeException(ErrorCode.VM_NOT_FOUND);
        });

    }

}
