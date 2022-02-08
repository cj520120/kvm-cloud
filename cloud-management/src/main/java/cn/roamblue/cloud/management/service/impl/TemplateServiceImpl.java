package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.TemplateInfo;
import cn.roamblue.cloud.management.bean.TemplateRefInfo;
import cn.roamblue.cloud.management.data.entity.TemplateEntity;
import cn.roamblue.cloud.management.data.entity.TemplateRefEntity;
import cn.roamblue.cloud.management.data.mapper.TemplateMapper;
import cn.roamblue.cloud.management.data.mapper.TemplateRefMapper;
import cn.roamblue.cloud.management.service.TemplateService;
import cn.roamblue.cloud.management.util.BeanConverter;
import cn.roamblue.cloud.management.util.TemplateStatus;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class TemplateServiceImpl extends AbstractService implements TemplateService {
    @Autowired
    private TemplateMapper templateRepository;
    @Autowired
    private TemplateRefMapper templateRefRepository;

    @Override
    public List<TemplateInfo> listTemplates() {

        List<TemplateEntity> entityList = templateRepository.selectAll();
        List<TemplateInfo> list = BeanConverter.convert(entityList, this::initTemplateInfo);
        return list;
    }

    @Override
    public List<TemplateInfo> search(int clusterId) {

        QueryWrapper<TemplateEntity> wrapper = new QueryWrapper<>();
        if (clusterId > 0) {
            wrapper.eq("cluster_id", clusterId);
        }
        List<TemplateEntity> entityList = templateRepository.selectList(wrapper);
        List<TemplateInfo> list = BeanConverter.convert(entityList, this::initTemplateInfo);
        BeanConverter.convert(entityList, this::initTemplateInfo);
        return list;
    }

    @Override
    public List<TemplateInfo> listTemplateByClusterId(int clusterId) {

        List<TemplateEntity> entityList = templateRepository.findByClusterId(clusterId);
        List<TemplateInfo> list = BeanConverter.convert(entityList, this::initTemplateInfo);
        return list;
    }

    @Override
    public TemplateInfo findTemplateById(int id) {

        TemplateEntity entity = templateRepository.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.TEMPLATE_NOT_FOUND, "模版不存在");
        }
        TemplateInfo info = initTemplateInfo(entity);
        return info;
    }

    @Override
    public TemplateInfo createTemplate(int clusterId, int osCategoryId, String name, String type, String uri) {

        TemplateEntity entity = TemplateEntity.builder()
                .clusterId(clusterId)
                .templateName(name)
                .templateType(type)
                .osCategoryId(osCategoryId)
                .templateUri(uri)
                .templateStatus(TemplateStatus.READY)
                .createTime(new Date())
                .templateSize(0L)
                .build();
        templateRepository.insert(entity);
        TemplateInfo info = initTemplateInfo(entity);
        log.info("create template success.info={}", info);
        return info;
    }

    @Override
    public void destroyTemplateById(int id) {

        templateRepository.deleteById(id);
        templateRefRepository.deleteByTemplateId(id);
        log.info("destroy template success.id={}", id);

    }


    @Override
    public List<TemplateRefInfo> listTemplateRefByTemplateId(int templateId) {

        List<TemplateRefEntity> list = this.templateRefRepository.selectList(new QueryWrapper<TemplateRefEntity>().eq("template_id", templateId));
        if (list.isEmpty()) {
            throw new CodeException(ErrorCode.TEMPLATE_STORAGE_NOT_READY, "模版未就绪");
        }
        List<TemplateRefInfo> result = BeanConverter.convert(list, this::initTemplateRefInfo);
        return result;
    }


    private TemplateRefInfo initTemplateRefInfo(TemplateRefEntity entity) {
        return TemplateRefInfo.builder()
                .id(entity.getId())
                .clusterId(entity.getClusterId())
                .storageId(entity.getStorageId())
                .templateId(entity.getTemplateId())
                .target(entity.getTemplateTarget())
                .status(entity.getTemplateStatus())
                .createTime(entity.getCreateTime())
                .build();
    }

    private TemplateInfo initTemplateInfo(TemplateEntity entity) {
        String status = entity.getTemplateUri().startsWith("http") ? TemplateStatus.INIT : TemplateStatus.ERROR;
        if (templateRefRepository.selectCount(new QueryWrapper<TemplateRefEntity>().eq("template_id", entity.getId())) > 0) {
            status = TemplateStatus.READY;
        }
        return TemplateInfo.builder().clusterId(entity.getClusterId())
                .id(entity.getId())
                .name(entity.getTemplateName())
                .uri(entity.getTemplateUri())
                .type(entity.getTemplateType())
                .osCategoryId(entity.getOsCategoryId())
                .createTime(entity.getCreateTime())
                .status(status)
                .build();
    }
}
