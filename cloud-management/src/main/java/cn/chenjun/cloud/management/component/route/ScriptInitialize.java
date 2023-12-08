package cn.chenjun.cloud.management.component.route;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
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
public class ScriptInitialize implements RouteComponentQmaInitialize{
    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId) {
        List<GuestQmaRequest.QmaBody> commands=new ArrayList<>();
        //安装网络检测脚本
        String networkCheckScript = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/network_check_shell.tpl")), StandardCharsets.UTF_8);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/tmp/network_check.sh").fileBody(networkCheckScript).build())).build());

        //安装服务检测脚本
        String serviceCheckScript = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/check_install_service_shell.tpl")), StandardCharsets.UTF_8);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/tmp/check_install_service_shell.sh").fileBody(serviceCheckScript).build())).build());
        //安装服务检测脚本
        String checkPythonScript = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/check_python_shell.tpl")), StandardCharsets.UTF_8);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/tmp/check_python_install.sh").fileBody(checkPythonScript).build())).build());

        return commands;
    }

    @Override
    public int getOrder() {
        return RouteOrder.SCRIPT_INIT;
    }
}
