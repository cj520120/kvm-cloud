package cn.roamblue.cloud.management.task;

import cn.hutool.http.HttpUtil;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.TemplateEntity;
import cn.roamblue.cloud.management.data.entity.TemplateRefEntity;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.data.mapper.TemplateMapper;
import cn.roamblue.cloud.management.data.mapper.TemplateRefMapper;
import cn.roamblue.cloud.management.service.LockService;
import cn.roamblue.cloud.management.util.LockKeyUtil;
import cn.roamblue.cloud.management.util.TemplateStatus;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 模版检测下载
 *
 * @author chenjun
 */
@Slf4j
@Component
public class TemplateInitializeTask extends AbstractTask {

    @Autowired
    private TemplateMapper templateMapper;
    @Autowired
    private TemplateRefMapper templateRefMapper;

    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private LockService lockService;

    @Override
    protected int getInterval() {
        return this.config.getTemplateCheckInterval();
    }

    @Override
    protected String getName() {
        return "TemplateInitializeTask";
    }

    @Override
    protected void call() {
        List<TemplateEntity> list = templateMapper.selectAll();
        for (TemplateEntity template : list) {
            if (!template.getTemplateUri().startsWith("http")) {
                continue;
            }
            if (templateRefMapper.selectCount(new QueryWrapper<TemplateRefEntity>().eq("template_id", template.getId())) == 0) {
                lockService.tryRun(LockKeyUtil.getTemplateLockKey(template.getId()), () -> {
                    download(template);
                    return null;
                }, 1, TimeUnit.HOURS);

            }
        }
    }

    private void download(TemplateEntity template) {
        try {
            Optional<HostEntity> hostOptional = hostMapper.selectList(new QueryWrapper<HostEntity>().eq("cluster_id", template.getClusterId())).stream().findAny();
            if (!hostOptional.isPresent()) {
                return;
            }
            Optional<StorageEntity> storageOptional = storageMapper.selectList(new QueryWrapper<StorageEntity>().eq("cluster_id", template.getClusterId())).stream().findAny();
            if (!storageOptional.isPresent()) {
                return;
            }
            HostEntity hostEntity = hostOptional.get();
            StorageEntity storageEntity = storageOptional.get();
            TemplateRefEntity templateRefEntity = TemplateRefEntity.builder()
                    .templateId(template.getId())
                    .clusterId(template.getClusterId())
                    .storageId(storageEntity.getId())
                    .templateTarget(UUID.randomUUID().toString().replace("-", ""))
                    .createTime(new Date())
                    .build();
            Map<String, Object> map = new HashMap<>(2);
            map.put("path", "/mnt/" + storageEntity.getStorageTarget() + "/" + templateRefEntity.getTemplateTarget());
            map.put("uri", template.getTemplateUri());
            log.info("start downloading template.template={} path={} host={}", template, map.get("path"), hostEntity.getHostUri());
            Gson gson = new Gson();
            ResultUtil<Long> downloadResult = gson.fromJson(HttpUtil.post(hostEntity.getHostUri() + "/download/template", map), new TypeToken<ResultUtil<Long>>() {
            }.getType());
            if (downloadResult.getCode() == ErrorCode.SUCCESS) {
                template.setTemplateSize(downloadResult.getData());
                template.setTemplateStatus(TemplateStatus.READY);
                templateRefEntity.setTemplateStatus(TemplateStatus.READY);
                templateMapper.updateById(template);
                templateRefMapper.insert(templateRefEntity);
                log.info("template downloaded successfully.template={} ref={}", template, map.get("path"));
            } else {
                log.error("failed to download template.template={}.msg={}", template, downloadResult.getMessage());
            }
        } catch (Exception err) {
            log.error("failed to download template.template={}", template, err);
        }
    }
}
