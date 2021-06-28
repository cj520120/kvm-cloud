package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.StorageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface StorageMapper extends BaseMapper<StorageEntity> {
    /**
     * 获取存储池列表
     *
     * @return
     */
    @Select(value = "SELECT * FROM tbl_storage_info")
    List<StorageEntity> selectAll();

    /**
     * 根据集群ID获取存储池
     *
     * @param clusterId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_storage_info WHERE cluster_id=#{clusterId}")
    List<StorageEntity> findByClusterId(@Param("clusterId") int clusterId);
}
