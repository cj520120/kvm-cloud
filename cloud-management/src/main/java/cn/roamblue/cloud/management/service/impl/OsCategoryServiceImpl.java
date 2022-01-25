package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Rule;
import cn.roamblue.cloud.management.bean.OsCategoryInfo;
import cn.roamblue.cloud.management.data.entity.OsCategoryEntity;
import cn.roamblue.cloud.management.data.mapper.OsCategoryMapper;
import cn.roamblue.cloud.management.service.OsCategoryService;
import cn.roamblue.cloud.management.util.BeanConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author chenjun
 */
@Service
public class OsCategoryServiceImpl extends AbstractService implements OsCategoryService {
    @Autowired
    private OsCategoryMapper osCategoryMapper;

    @Override
    public OsCategoryInfo findOsCategoryById(int id) {
        OsCategoryEntity entity = osCategoryMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.OS_CATEGORY_NOT_FOUND, localeMessage.getMessage("OS_CATEGORY_NOT_FOUND", "操作系统类型未找到"));
        }
        return this.init(entity);
    }

    @Override
    public List<OsCategoryInfo> listAllOsCategory() {
        List<OsCategoryEntity> list = osCategoryMapper.selectAll();
        return BeanConverter.convert(list, this::init);
    }
    @Rule(permissions="category.create")
    @Override
    public OsCategoryInfo createOsCategory(String categoryName, String networkDriver, String diskDriver) {
        OsCategoryEntity entity = OsCategoryEntity.builder().categoryName(categoryName)
                .networkDriver(networkDriver)
                .diskDriver(diskDriver)
                .createTime(new Date())
                .build();

        this.osCategoryMapper.insert(entity);
        return this.init(entity);
    }

    @Rule(permissions="category.modify")
    @Override
    public OsCategoryInfo modifyOsCategory(int id, String categoryName, String diskDriver, String networkDriver) {
        OsCategoryEntity osCategoryEntity = osCategoryMapper.selectById(id);
        if (osCategoryEntity == null) {
            throw new CodeException(ErrorCode.OS_CATEGORY_NOT_FOUND, localeMessage.getMessage("OS_CATEGORY_NOT_FOUND", "操作系统类型未找到"));
        }
        osCategoryEntity.setDiskDriver(diskDriver);
        osCategoryEntity.setNetworkDriver(networkDriver);
        osCategoryEntity.setCategoryName(categoryName);
        this.osCategoryMapper.updateById(osCategoryEntity);
        return this.init(osCategoryEntity);
    }

    @Rule(permissions="category.destroy")
    @Override
    public void destroyOsCategoryById(int id) {
        this.osCategoryMapper.deleteById(id);
    }

    private OsCategoryInfo init(OsCategoryEntity entity) {
        return OsCategoryInfo.builder()
                .id(entity.getId())
                .categoryName(entity.getCategoryName())
                .diskDriver(entity.getDiskDriver())
                .networkDriver(entity.getNetworkDriver())
                .createTime(entity.getCreateTime())
                .build();
    }
}
