package cn.chenjun.cloud.management.component.route;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.mapper.GuestNetworkMapper;
import cn.chenjun.cloud.management.util.TemplateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author chenjun
 */
@Component
public class NginxInitialize implements RouteComponentQmaInitialize {

    @Autowired
    private GuestNetworkMapper guestNetworkMapper;

    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId, Map<String, Object> sysconfig) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        List<String> ipList = new ArrayList<>();
        ipList.add("169.254.169.254");
        String nginxConfig = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/meta/nginx.tpl").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        Map<String, Object> map = new HashMap<>(0);
        map.put("__SYS__", sysconfig);
        map.put("ipList", ipList);
        nginxConfig = TemplateUtil.create().render(nginxConfig, map);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("sh").args(new String[]{"/tmp/check_install_service_shell.sh", "nginx"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/nginx/nginx.conf").fileBody(nginxConfig).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "nginx"}).checkSuccess(true).build())).build());
        return commands;
    }

    @Override
    public int getOrder() {
        return ComponentOrder.NGINX;
    }
}
