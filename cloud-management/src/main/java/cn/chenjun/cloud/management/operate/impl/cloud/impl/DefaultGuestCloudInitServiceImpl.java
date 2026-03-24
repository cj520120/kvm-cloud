package cn.chenjun.cloud.management.operate.impl.cloud.impl;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.MapUtil;
import cn.chenjun.cloud.management.data.dao.TemplateDao;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.TemplateEntity;
import cn.chenjun.cloud.management.operate.impl.cloud.CloudInitService;
import cn.chenjun.cloud.management.operate.impl.cloud.bean.CloudData;
import cn.chenjun.cloud.management.util.CloudInitIsoGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class DefaultGuestCloudInitServiceImpl implements CloudInitService {
    @Autowired
    private TemplateDao templateDao;

    @Override
    public CloudData build(GuestEntity guest, HostEntity host) {
        TemplateEntity template = this.templateDao.findById(guest.getTemplateId());
        if (template == null) {
            return null;
        }
        if (ObjectUtils.isEmpty(template.getLocalCloudCfg())) {
            return CloudData.builder().data(null).waiting(Objects.equals(template.getCloudWaitFlag(), Constant.CloudWaitFlag.YES)).build();
        }
        try {
            Yaml yaml = new Yaml();
            Object object = yaml.load(template.getLocalCloudCfg());
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            options.setIndent(2);
            options.setPrettyFlow(true);
            Yaml prettyYaml = new Yaml(options);
            String userData = "#cloud-config\n" + prettyYaml.dump(object);
            Map<String, Object> metaDataMap = MapUtil.of("instance-id", guest.getName());
            String metaData = "#cloud-config\n" + prettyYaml.dump(metaDataMap);
            CloudInitIsoGenerator cloudInitIsoGenerator = new CloudInitIsoGenerator();
            String data = cloudInitIsoGenerator.generateCloudInitImage(userData, metaData, null, null);
            return CloudData.builder().data(data).waiting(Objects.equals(template.getCloudWaitFlag(), Constant.CloudWaitFlag.YES)).build();
        } catch (Exception e) {
            log.error("parse cloud init script error", e);
        }
        return null;
    }

    @Override
    public int getSupportType() {
        return Constant.GuestType.USER;
    }
}
