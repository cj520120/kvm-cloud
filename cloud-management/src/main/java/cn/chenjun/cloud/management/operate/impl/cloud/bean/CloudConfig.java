package cn.chenjun.cloud.management.operate.impl.cloud.bean;

import cn.chenjun.cloud.common.util.ResourceUtil;
import lombok.Data;

import java.util.*;

@Data
public class CloudConfig {

    private Map<String, Object> userData = new LinkedHashMap<>();
    private Map<String, Object> networks = new LinkedHashMap<>();
    private Map<String, Object> metaData = new LinkedHashMap<>();

    public void appendUserData(String key, Object value) {
        this.userData.put(key, value);
    }

    public void addUser(Object user) {
        List<Object> users = (List<Object>) this.userData.computeIfAbsent("users", k -> new ArrayList<>());
        users.add(user);
    }

    public void addMetaData(String key, Object value) {
        this.metaData.put(key, value);
    }

    public void appendFile(String filePath, String content) {
        List<Map<String, Object>> files = (List<Map<String, Object>>) this.userData.computeIfAbsent("write_files", k -> new ArrayList<Map<String, Object>>());
        Map<String, Object> file = new LinkedHashMap<>();
        file.put("permissions", "0644");
        file.put("owner", "root:root");
        file.put("encoding", "b64");
        file.put("path", filePath);
        file.put("content", Base64.getEncoder().encodeToString(content.getBytes()));
        files.add(file);
    }

    public void appendResourceFile(String filePath, String resourcePath) {

        this.appendFile(filePath, ResourceUtil.readUtf8Str(resourcePath));

    }

    public void appendRuncmd(String command) {
        List<String> commands = (List<String>) this.userData.computeIfAbsent("runcmd", k -> new ArrayList<String>());
        if (!commands.contains(command)) {
            commands.add(command);
        }
    }

    public void appendPackage(String packageName) {
        List<String> packages = (List<String>) this.userData.computeIfAbsent("packages", k -> new ArrayList<String>());
        if (!packages.contains(packageName)) {
            packages.add(packageName);
        }
    }

    public void addNetwork(String name, Map<String, Object> network) {
        Map<String, Object> networkMap = (Map<String, Object>) this.networks.get("network");
        if (networkMap == null) {
            networkMap = new HashMap<>();
            this.networks.put("network", networkMap);
        }
        Map<String, Object> ethernets = (Map<String, Object>) networkMap.get("ethernets");
        if (ethernets == null) {
            ethernets = new HashMap<>();
            networkMap.put("ethernets", ethernets);
            networkMap.put("version", 2);
        }
        ethernets.put(name, network);
    }

}
