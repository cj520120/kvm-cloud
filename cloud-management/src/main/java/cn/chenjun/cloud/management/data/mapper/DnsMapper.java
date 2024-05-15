package cn.chenjun.cloud.management.data.mapper;

import cn.chenjun.cloud.management.data.entity.DnsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface DnsMapper extends BaseMapper<DnsEntity> {

    /**
     * 查询网络配置的dns信息
     *
     * @param networkId
     * @return
     */
    @Select("select * from tbl_dns_info where network_id = #{networkId}")
    List<DnsEntity> findByNetworkId(@Param("networkId") int networkId);
}
