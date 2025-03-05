package cn.chenjun.cloud.management.component.global;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.component.route.ComponentOrder;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.util.ConfigKey;
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
public class YumSourceCommand implements GlobalComponentQmaInitialize {
    @Autowired
    protected ConfigService configService;

    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId, Map<String, Object> sysconfig) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        String source = configService.getConfig(ConfigKey.SYSTEM_COMPONENT_YUM_INSTALL_SOURCE);
        source = source.replace("\r", "").trim();
        if (!StringUtils.isEmpty(source)) {
            //写入yum源
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/yum.repos.d/oracle-linux-ol9.repo").fileBody(source).build())).build());
            //清除yum缓存
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"clean", "all"}).checkSuccess(true).build())).build());
            //重新生成yum缓存文件
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"makecache"}).checkSuccess(true).build())).build());
        }
        return commands;
    }

    @Override
    public int getOrder() {
        return ComponentOrder.YUM_SOURCE;
    }
}
