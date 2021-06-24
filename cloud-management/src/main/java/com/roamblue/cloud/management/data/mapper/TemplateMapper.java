package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.TemplateEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface TemplateMapper extends BaseMapper<TemplateEntity> {
    /**
     * 获取模版列表
     *
     * @return
     */
    @Select(value = "SELECT * FROM tbl_template_info")
    List<TemplateEntity> selectAll();

    /**
     * 根据集群ID获取模版
     *
     * @param clusterId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_template_info WHERE cluster_id=#{clusterId}")
    List<TemplateEntity> findByClusterId(@Param("clusterId") int clusterId);
}
