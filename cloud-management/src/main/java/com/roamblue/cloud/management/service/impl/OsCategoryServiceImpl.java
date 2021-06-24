package com.roamblue.cloud.management.service.impl;

import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.bean.OsCategoryInfo;
import com.roamblue.cloud.management.data.entity.OsCategoryEntity;
import com.roamblue.cloud.management.data.mapper.OsCategoryMapper;
import com.roamblue.cloud.management.service.OsCategoryService;
import com.roamblue.cloud.management.util.BeanConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class OsCategoryServiceImpl implements OsCategoryService {
    @Autowired
    private OsCategoryMapper osCategoryMapper;

    @Override
    public OsCategoryInfo findOsCategoryById(int id) {
        OsCategoryEntity entity = osCategoryMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.OS_CATEGORY_NOT_FOUND, "操作系统模版未找到");
        }
        return this.init(entity);
    }

    @Override
    public List<OsCategoryInfo> listAllOsCategory() {
        List<OsCategoryEntity> list = osCategoryMapper.selectAll();
        return BeanConverter.convert(list, this::init);
    }

    private OsCategoryInfo init(OsCategoryEntity entity) {
        return OsCategoryInfo.builder()
                .id(entity.getId())
                .categoryName(entity.getCategoryName())
                .diskDriver(entity.getDiskDriver())
                .networkDriver(entity.getNetworkDriver())
                .build();
    }
}
