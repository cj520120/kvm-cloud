package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.dao.DnsDao;
import cn.chenjun.cloud.management.data.entity.DnsEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.model.DnsModel;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author chenjun
 */
@Service
public class DnsService extends AbstractService {
    @Autowired
    private DnsDao dnsDao;

    public List<DnsEntity> listDnsByNetworkId(int networkId) {
        List<DnsEntity> entityList = this.dnsDao.listByNetworkId(networkId);
        return entityList;
    }

    public Page<DnsEntity> search(int networkId, String keyword, int no, int size) {

        Page<DnsEntity> page = this.dnsDao.search(networkId, keyword, no, size);
        return page;
    }

    public List<DnsEntity> listLocalNetworkDns(int networkId) {
        NetworkEntity network = networkDao.findById(networkId);
        if (network == null) {
            return new ArrayList<>();
        }
        List<DnsModel> list = new ArrayList<>();
        List<GuestEntity> guestList = this.guestDao.listByNetworkId(networkId);
        for (GuestEntity guest : guestList) {
            list.add(DnsModel.builder().domain(guest.getName() + "." + network.getDomain()).ip(guest.getGuestIp()).build());
        }
        return this.dnsDao.listByNetworkId(networkId);
    }

    public void deleteDns(int dnsId) {
        DnsEntity entity = this.dnsDao.findById(dnsId);
        if (entity != null) {
            this.dnsDao.deleteById(dnsId);
            NotifyContextHolderUtil.append(NotifyData.<List<DnsModel>>builder().id(entity.getNetworkId()).type(Constant.NotifyType.UPDATE_COMPONENT_DNS).build());
        }
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(dnsId).type(Constant.NotifyType.UPDATE_DNS).build());

    }

    public DnsEntity createDns(int networkId, String domain, String ip) {
        DnsEntity entity = DnsEntity.builder().dnsIp(ip).dnsDomain(domain).networkId(networkId).createTime(new Date()).build();
        dnsDao.insert(entity);
        NotifyContextHolderUtil.append(NotifyData.<List<DnsModel>>builder().id(networkId).type(Constant.NotifyType.UPDATE_COMPONENT_DNS).build());
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(entity.getDnsId()).type(Constant.NotifyType.UPDATE_DNS).build());
        return entity;
    }

    public DnsEntity getDnsInfo(int dnsId) {
        DnsEntity entity = this.dnsDao.findById(dnsId);
        if (entity == null) {
            throw new CodeException(ErrorCode.DNS_NOT_FOUND, "dns不存在");
        }
        return entity;
    }


}
