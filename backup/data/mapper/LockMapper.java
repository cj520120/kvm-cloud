package cn.roamblue.cloud.management.data.mapper;

import cn.roamblue.cloud.management.data.entity.LockEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author chenjun
 */
@Repository
public interface LockMapper extends BaseMapper<LockEntity> {
    /**
     * 获取锁信息
     *
     * @param name
     * @return
     */
    @Select(value = "SELECT * FROM tbl_lock_info WHERE lock_name=#{name}")
    LockEntity findByName(@Param("name") String name);
}
