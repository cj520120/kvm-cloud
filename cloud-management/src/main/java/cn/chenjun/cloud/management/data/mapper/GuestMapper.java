package cn.chenjun.cloud.management.data.mapper;

import cn.chenjun.cloud.management.data.entity.GuestEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface GuestMapper extends BaseMapper<GuestEntity> {
    /**
     * 删除模版后取消挂载
     *
     * @param templateId
     */
    @Update("update tbl_guest_info set guest_cd_room=0 where guest_cd_room=#{templateId}")
    void detachCdByTemplateId(@Param("templateId") int templateId);

    /**
     * 获取网络下的所有主机
     * @param networkId
     * @return
     */
    @Select("select * from tbl_guest_info where network_id=${networkId}")
    List<GuestEntity> findGuestByNetworkId(@Param("networkId") int networkId);
}
