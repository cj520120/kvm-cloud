package cn.chenjun.cloud.management.component.global;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.component.route.ComponentOrder;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.hutool.core.io.resource.ResourceUtil;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
/**
 * @author chenjun
 */
@Component
public class ScriptInitialize implements GlobalComponentQmaInitialize {
    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId) {
        List<GuestQmaRequest.QmaBody> commands=new ArrayList<>();
        //安装网络检测脚本
        String networkCheckScript = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/script/network_check_shell.tpl")), StandardCharsets.UTF_8);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/tmp/network_check.sh").fileBody(networkCheckScript).build())).build());


        String serviceCheckScript = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/script/check_install_service_shell.tpl")), StandardCharsets.UTF_8);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/tmp/check_install_service_shell.sh").fileBody(serviceCheckScript).build())).build());

        String checkPythonScript = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/script/check_python_shell.tpl")), StandardCharsets.UTF_8);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/tmp/check_python_install.sh").fileBody(checkPythonScript).build())).build());

        String firewalldScript = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/script/open_firewalld_shell.tpl")), StandardCharsets.UTF_8);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/tmp/open_firewalld.sh").fileBody(firewalldScript).build())).build());

        String openFirewalldScript = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/script/open_firewalld_shell.tpl")), StandardCharsets.UTF_8);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/tmp/open_firewalld.sh").fileBody(openFirewalldScript).build())).build());

        return commands;
    }

    @Override
    public int getOrder() {
        return ComponentOrder.SCRIPT_INIT;
    }
}
