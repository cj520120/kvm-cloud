package cn.chenjun.cloud.management.component.route;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.hutool.core.io.resource.ResourceUtil;
import com.hubspot.jinjava.Jinjava;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author chenjun
 */
@Component
public class CloudServiceInitialize implements RouteComponentQmaInitialize {
    @Autowired
    private ApplicationConfig applicationConfig;
    @Autowired
    private NetworkMapper networkMapper;

    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId) {
        NetworkEntity network = networkMapper.selectById(component.getNetworkId());
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("mkdir").args(new String[]{"-p", "/usr/local/cloud-service/"}).checkSuccess(true).build())).build());
        String cloudServiceShell = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/cloud_shell.tpl").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        String cloudService = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/cloud_service.tpl").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        String cloudPython = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/cloud_py.tpl").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        Map<String, Object> map = new HashMap<>(3);
        map.put("managerUri", applicationConfig.getManagerUri());
        map.put("secret", network.getSecret());
        map.put("networkId", network.getNetworkId());
        Jinjava jinjava = new Jinjava();
        cloudPython = jinjava.render(cloudPython, map);

        cloudPython = String.format(cloudPython, applicationConfig.getManagerUri(), network.getSecret(), network.getNetworkId());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/usr/local/cloud-service/cloud.py").fileBody(cloudPython).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/usr/local/cloud-service/service.sh").fileBody(cloudServiceShell).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/usr/lib/systemd/system/cloud-service.service").fileBody(cloudService).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("chmod").args(new String[]{"a+x", "/usr/local/cloud-service/service.sh"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"daemon-reload"}).checkSuccess(true).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"enable", "cloud-service"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "cloud-service"}).build())).build());
        return commands;
    }

    @Override
    public int getOrder() {
        return RouteOrder.CLOUD;
    }
}
