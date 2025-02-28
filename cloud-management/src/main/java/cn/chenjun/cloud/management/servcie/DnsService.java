package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.DnsEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.DnsMapper;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.model.DnsModel;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
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
        List<DnsEntity> entityList = this.mapper.findByNetworkId(networkId);
        return ResultUtil.<List<DnsModel>>builder().data(entityList.stream().map(this::initDns).collect(Collectors.toList())).build();
    }


    public List<DnsModel> listLocalNetworkDns(int networkId) {
        NetworkEntity network = networkMapper.selectById(networkId);
        if (network == null) {
            return new ArrayList<>();
        }
        List<DnsModel> list = new ArrayList<>();
        List<GuestEntity> guestList = this.guestMapper.findGuestByNetworkId(networkId);
        for (GuestEntity guest : guestList) {
            list.add(DnsModel.builder().domain(guest.getName() + "." + network.getDomain()).ip(guest.getGuestIp()).build());
        }
        List<DnsEntity> entityList = this.mapper.findByNetworkId(networkId);
        for (DnsEntity entity : entityList) {
            list.add(this.initDns(entity));
        }
        return list;
    }

    public ResultUtil<Void> deleteDns(int dnsId) {
        DnsEntity entity = this.mapper.selectById(dnsId);
        if (entity != null) {
            this.mapper.deleteById(dnsId);
            this.notifyService.publish(NotifyData.<List<DnsModel>>builder().id(entity.getNetworkId()).type(Constant.NotifyType.COMPONENT_UPDATE_DNS).data(this.listLocalNetworkDns(entity.getNetworkId())).build());
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(dnsId).type(Constant.NotifyType.UPDATE_DNS).build());
        return ResultUtil.success();
    }

    public ResultUtil<DnsModel> createDns(int networkId, String domain, String ip) {
        DnsEntity entity = DnsEntity.builder().dnsIp(ip).dnsDomain(domain).networkId(networkId).createTime(new Date()).build();
        mapper.insert(entity);
        this.notifyService.publish(NotifyData.<List<DnsModel>>builder().id(networkId).type(Constant.NotifyType.COMPONENT_UPDATE_DNS).data(this.listLocalNetworkDns(entity.getNetworkId())).build());
        this.notifyService.publish(NotifyData.<Void>builder().id(entity.getDnsId()).type(Constant.NotifyType.UPDATE_DNS).build());
        return ResultUtil.<DnsModel>builder().data(this.initDns(entity)).build();
    }

    public ResultUtil<DnsModel> getDnsInfo(int dnsId) {
        DnsEntity entity = this.mapper.selectById(dnsId);
        if (entity == null) {
            return ResultUtil.error(ErrorCode.DNS_NOT_FOUND, "dns不存在");
        }
        return ResultUtil.<DnsModel>builder().data(this.initDns(entity)).build();
    }

    private DnsModel initDns(DnsEntity entity) {
        return DnsModel.builder().id(entity.getDnsId())
                .networkId(entity.getNetworkId())
                .domain(entity.getDnsDomain())
                .ip(entity.getDnsIp())
                .build();
    }
}
