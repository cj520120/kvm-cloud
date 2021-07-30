package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Rule;
import cn.roamblue.cloud.management.bean.HostInfo;
import cn.roamblue.cloud.management.service.HostService;
import cn.roamblue.cloud.management.ui.HostUiService;
import cn.roamblue.cloud.management.util.RuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author chenjun
 */
@Service
public class HostUiServiceImpl extends AbstractUiService implements HostUiService {
    @Autowired
    private HostService hostService;

    @Override
    public ResultUtil<List<HostInfo>> listHost() {
        return super.call(() -> hostService.listHost());
    }

    @Override
    public ResultUtil<List<HostInfo>> search(int clusterId) {
        return super.call(() -> hostService.search(clusterId));
    }

    @Override
    public ResultUtil<HostInfo> findHostById(int id) {
        return super.call(() -> hostService.findHostById(id));

    }

    @Rule(min = RuleType.ADMIN)
    @Override
    public ResultUtil<HostInfo> updateHostStatusById(int id, String status) {

        if (StringUtils.isEmpty(status)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "状态不能为空");
        }
        return super.call(() -> hostService.updateHostStatusById(id,status));
    }

    @Override
    @Rule(min = RuleType.ADMIN)
    public ResultUtil<HostInfo> createHost(int clusterId, String name, String ip, String uri) {

        if (StringUtils.isEmpty(name)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "名称不能为空");
        }

        if (StringUtils.isEmpty(ip)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "主机地址不能为空");
        }

        if (StringUtils.isEmpty(uri)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "通信地址不能为空");
        }
        if (clusterId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "集群不能为空");
        }
        return super.call(() -> hostService.createHost(clusterId, name, ip, uri));

    }

    @Override
    @Rule(min = RuleType.ADMIN)
    public ResultUtil<Void> destroyHostById(int id) {
        return super.call(() -> hostService.destroyHostById(id));
    }

}
