package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface VolumeMapper extends BaseMapper<VolumeEntity> {
    /**
     * 获取磁盘信息
     *
     * @return
     */
    @Select(value = "SELECT * FROM tbl_volume_info")
    List<VolumeEntity> selectAll();

    /**
     * 根据集群获取磁盘
     *
     * @param clusterId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_volume_info WHERE cluster_id=#{clusterId}")
    List<VolumeEntity> findByClusterId(@Param("clusterId") int clusterId);

    /**
     * 获取VM磁盘
     *
     * @param vmId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_volume_info WHERE vm_id=#{vmId} ORDER BY vm_device ASC")
    List<VolumeEntity> findByVmId(@Param("vmId") int vmId);

    /**
     * 根据存储池获取磁盘卷
     *
     * @param storageId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_volume_info WHERE storage_id=#{storageId} ")
    List<VolumeEntity> findByStorageId(@Param("storageId") int storageId);

}
