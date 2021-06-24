package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.TemplateRefEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * @author chenjun
 */
@Repository
public interface TemplateRefMapper extends BaseMapper<TemplateRefEntity> {

    /**
     * 根据存储池ID删除模版引用
     *
     * @param storageId
     */
    @Update("DELETE FROM tbl_template_ref_info where storage_id=#{storageId}")
    void deleteByStorageId(@Param("storageId") int storageId);

    /**
     * 根据模版ID删除模版引用
     *
     * @param templateId
     */
    @Update("DELETE FROM tbl_template_ref_info where template_id=#{templateId}")
    void deleteByTemplateId(@Param("templateId") int templateId);
}
