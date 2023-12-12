package cn.chenjun.cloud.management.component.global;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.component.route.ComponentOrder;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenjun
 */
@Component
public class PipInitialize implements GlobalComponentQmaInitialize {
    @Autowired
    private ApplicationConfig applicationConfig;

    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("sh").args(new String[]{"/tmp/check_python_install.sh"}).checkSuccess(true).build())).build());
        commands.add(this.pip3Install("flask"));
        commands.add(this.pip3Install("requests"));
        commands.add(this.pip3Install("websocket-client"));
        commands.add(this.pip3Install("websockify==0.10.0"));
        return commands;
    }

    protected GuestQmaRequest.QmaBody pip3Install(String soft) {
        String source = applicationConfig.getPipSource().replace("\r", "").trim();
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
