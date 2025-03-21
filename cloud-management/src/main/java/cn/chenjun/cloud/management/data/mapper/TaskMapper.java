package cn.chenjun.cloud.management.data.mapper;

import cn.chenjun.cloud.management.data.entity.TaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author chenjun
 */
@Repository
public interface TaskMapper extends BaseMapper<TaskEntity> {

    /**
     * 保活
     *
     * @param taskId
     * @param expireTime
     */
    @Update("update tbl_task_info set task_version=task_version+1,expire_time=#{expireTime} where task_id=#{taskId}")
    void keep(@Param("taskId") String taskId, @Param("expireTime") Date expireTime);

    /**
     * 更新数据版本
     *
     * @param taskId
     * @param oldVersion
     * @param expireTime
     * @return
     */
    @Update("update tbl_task_info set task_version=task_version+1,expire_time=#{expireTime} where task_id=#{taskId} and task_version=#{oldVersion}")
    int updateVersion(@Param("taskId") String taskId, @Param("oldVersion") int oldVersion, @Param("expireTime") Date expireTime);

}
