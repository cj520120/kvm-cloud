package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.ManagementEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author chenjun
 */
@Repository
public interface ManagementMapper extends BaseMapper<ManagementEntity> {
    /**
     * 查询管理端
     *
     * @param serverId
     * @return
     */
    @Select("SELECT * FROM tbl_management_info WHERE server_id=#{serverId}")
    ManagementEntity findByServerId(@Param("serverId") String serverId);
}
