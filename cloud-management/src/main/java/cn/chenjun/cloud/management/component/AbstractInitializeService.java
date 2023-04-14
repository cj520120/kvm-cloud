package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenjun
 */
public abstract class AbstractInitializeService {
    @Autowired
    protected ApplicationConfig applicationConfig;

    protected List<GuestQmaRequest.QmaBody> initYumSource() {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        String source = applicationConfig.getYumSource().replace("\r", "").trim();
        if (!StringUtils.isEmpty(source)) {
            //写入yum源
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/yum.repos.d/CentOS-Base.repo").fileBody(applicationConfig.getYumSource().replace("\r", "")).build())).build());
            //清除yum缓存
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"clean", "all"}).checkSuccess(true).build())).build());
            //重新生成yum缓存文件
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"makecache"}).checkSuccess(true).build())).build());
        }
        return commands;
    }


    protected GuestQmaRequest.QmaBody pip3Install(String soft, boolean checkSuccess) {
        String source = applicationConfig.getPipSource().replace("\r", "").trim();
        if (StringUtils.isEmpty(source)) {
            return GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("pip3").args(new String[]{"install", soft}).checkSuccess(checkSuccess).build())).build();
        } else {

            return GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("pip3").args(new String[]{"install", "-i", source, soft}).checkSuccess(checkSuccess).build())).build();
        }
    }

}
