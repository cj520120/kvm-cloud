package cn.chenjun.cloud.management.component.global;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.component.route.ComponentOrder;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
@Component
public class PipInitialize implements GlobalComponentQmaInitialize {
    @Autowired
    private ConfigService configService;

    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId, Map<String, Object> sysconfig) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("sh").args(new String[]{"/tmp/check_python_install.sh"}).checkSuccess(true).build())).build());
        commands.add(this.pip3Install("flask"));
        commands.add(this.pip3Install("requests"));
        commands.add(this.pip3Install("websocket-client"));
        return commands;
    }

    protected GuestQmaRequest.QmaBody pip3Install(String soft) {

        String source = configService.getConfig(Constant.ConfigKey.SYSTEM_COMPONENT_PIP_INSTALL_SOURCE);
        source = source.replace("\r", "").trim();
        if (StringUtils.isEmpty(source)) {
            return GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("pip3").args(new String[]{"install", soft}).checkSuccess(true).build())).build();
        } else {
            return GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("pip3").args(new String[]{"install", "-i", source, soft}).checkSuccess(true).build())).build();
        }
    }

    @Override
    public int getOrder() {
        return ComponentOrder.PYTHON_INSTALL;
    }
}
