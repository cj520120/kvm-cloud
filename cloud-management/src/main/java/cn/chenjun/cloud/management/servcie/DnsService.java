package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.NotifyMessage;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.DnsEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.DnsMapper;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.model.DnsModel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DnsService {
    @Autowired
    private DnsMapper mapper;
    @Autowired
    private NetworkMapper networkMapper;

    @Autowired
    private GuestMapper guestMapper;
    @Autowired
    private NotifyService notifyService;

    public ResultUtil<List<DnsModel>> listDnsByNetworkId(int networkId) {
        QueryWrapper<DnsEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("network_id", networkId);
        List<DnsEntity> entityList = this.mapper.selectList(wrapper);
        return ResultUtil.<List<DnsModel>>builder().data(entityList.stream().map(this::initDns).collect(Collectors.toList())).build();
    }


    public List<DnsModel> listLocalNetworkDns(int networkId) {
        NetworkEntity network = networkMapper.selectById(networkId);
        if (network == null) {
            return new ArrayList<>();
        }
        List<DnsModel> list = new ArrayList<>();
        List<GuestEntity> guestList = this.guestMapper.selectList(new QueryWrapper<GuestEntity>().eq("network_id", networkId));
        for (GuestEntity guest : guestList) {
            list.add(DnsModel.builder().domain(guest.getName() + "." + network.getDomain()).ip(guest.getGuestIp()).build());
        }
        List<DnsEntity> entityList = this.mapper.selectList(new QueryWrapper<DnsEntity>().eq("network_id", networkId));
        for (DnsEntity entity : entityList) {
            list.add(this.initDns(entity));
        }
        return list;
    }
    public ResultUtil<Void> deleteDns(int dnsId) {
        DnsEntity entity = this.mapper.selectById(dnsId);
        if (entity != null) {
            this.mapper.deleteById(dnsId);
            this.notifyService.publish(NotifyMessage.builder().id(entity.getNetworkId()).type(Constant.NotifyType.COMPONENT_UPDATE_DNS).data(this.listLocalNetworkDns(entity.getNetworkId())).build());
        }
        return ResultUtil.success();
    }

    public ResultUtil<DnsModel> createDns(int networkId, String domain, String ip) {
        DnsEntity entity = DnsEntity.builder().dnsIp(ip).dnsDomain(domain).networkId(networkId).createTime(new Date()).build();
        mapper.insert(entity);
        this.notifyService.publish(NotifyMessage.builder().id(networkId).type(Constant.NotifyType.COMPONENT_UPDATE_DNS).data(this.listLocalNetworkDns(entity.getNetworkId())).build());
        return ResultUtil.<DnsModel>builder().data(this.initDns(entity)).build();
    }

    private DnsModel initDns(DnsEntity entity) {
        return DnsModel.builder().id(entity.getDnsId())
                .domain(entity.getDnsDomain())
                .ip(entity.getDnsIp())
                .build();
    }
}
