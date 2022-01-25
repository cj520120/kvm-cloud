package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.VmInfo;
import cn.roamblue.cloud.management.bean.VmStatisticsInfo;
import cn.roamblue.cloud.management.bean.VncInfo;
import cn.roamblue.cloud.management.data.entity.VmEntity;
import cn.roamblue.cloud.management.data.entity.VmStaticsEntity;
import cn.roamblue.cloud.management.data.mapper.VmMapper;
import cn.roamblue.cloud.management.data.mapper.VmStatsMapper;
import cn.roamblue.cloud.management.service.InstanceService;
import cn.roamblue.cloud.management.service.VmService;
import cn.roamblue.cloud.management.service.VncService;
import cn.roamblue.cloud.management.util.BeanConverter;
import cn.roamblue.cloud.management.util.TimeUtil;
import cn.roamblue.cloud.management.util.VmStatus;
import cn.roamblue.cloud.management.util.VmType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Primary
@Service
public class InstanceServiceImpl extends AbstractService implements InstanceService {

    @Autowired
    private VmMapper vmMapper;
    @Autowired
    private VmStatsMapper vmStatsMapper;
    @Autowired
    private VncService vncService;
    @Autowired
    private List<VmService> vmServiceList;

    @Override
    public VmService getVmServiceByVmId(int id) {
        VmEntity entity = vmMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, localeMessage.getMessage("VM_NOT_FOUND", "VM不存在"));
        }
        return getVmServiceByType(entity.getVmType());
    }

    @Override
    public VmService getVmServiceByType(String type) {
        VmService vmService = vmServiceList.stream().filter(t -> t.getType().equals(type)).findFirst().orElse(null);
        if (vmService == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, String.format(localeMessage.getMessage("UNKNOWN_VM_TYPE", "未知的实例类型:%s"), type));
        }
        return vmService;
    }

    @Override
    public VmInfo findVmById(int id) {

        VmEntity entity = vmMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, localeMessage.getMessage("VM_NOT_FOUND", "VM不存在"));
        }
        return this.initInstanceInfo(entity);
    }

    @Override
    public List<VmInfo> search(int clusterId, int hostId, int groupId, String type, String status) {

        QueryWrapper<VmEntity> wrapper = new QueryWrapper<>();
        if (clusterId > 0) {
            wrapper.eq("cluster_id", clusterId);
        }
        if (hostId > 0) {
            wrapper.eq("host_id", hostId);
        }
        if (groupId >= 0) {
            wrapper.eq("group_id", groupId);
        }
        if (!StringUtils.isEmpty(type)) {
            if (type.equalsIgnoreCase(VmType.GUEST)) {
                wrapper.eq("vm_type", VmType.GUEST);
            } else {
                wrapper.ne("vm_type", VmType.GUEST);
            }
        }
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("vm_status", status);
        }
        List<VmEntity> entityList = this.vmMapper.selectList(wrapper);
        List<VmInfo> list = BeanConverter.convert(entityList, this::initInstanceInfo);
        Collections.sort(list, (o1, o2) -> {
            int val1 = VmStatus.getCompareValue(o1.getStatus());
            int val2 = VmStatus.getCompareValue(o2.getStatus());
            int result = Integer.compare(val1, val2);
            if (result == 0) {
                result = o1.getDescription().compareTo(o2.getDescription());
            }
            return result;
        });
        return list;
    }

    @Override
    public List<VmInfo> listAllVm() {

        List<VmEntity> entityList = this.vmMapper.selectAll();
        List<VmInfo> list = BeanConverter.convert(entityList, this::initInstanceInfo);
        return list;
    }


    @Override
    public VncInfo findVncById(int vmId) {

        VmEntity vm = vmMapper.selectById(vmId);
        if (vm == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, localeMessage.getMessage("VM_NOT_FOUND", "VM不存在"));
        }
        if (vm.getVmStatus().equals(VmStatus.RUNNING)) {
            return this.vncService.findVncByVmId(vm.getClusterId(), vm.getId());

        } else {
            throw new CodeException(ErrorCode.VM_NOT_START, localeMessage.getMessage("VM_NOT_START", "虚拟机未启动"));
        }
    }


    @Override
    public List<VmStatisticsInfo> listVmStatisticsById(int vmId) {

        long startTime = System.currentTimeMillis() - 30 * 60 * 1000;
        List<Date> timeList = TimeUtil.getIntervalTimeList(new Date(startTime), new Date(), 10);
        Date starTime = timeList.remove(0);
        long diskWriteSpeed = 0L;
        long diskReadSpeed = 0L;
        long networkSendSpeed = 0L;
        long networkReceiveSpeed = 0L;
        int cpuUsage = 0;
        QueryWrapper<VmStaticsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vm_id", vmId);
        queryWrapper.gt("create_time", starTime);
        List<VmStaticsEntity> entityList = this.vmStatsMapper.selectList(queryWrapper);

        List<VmStatisticsInfo> list = new ArrayList<>(timeList.size());
        for (Date time : timeList) {
            long start = starTime.getTime();
            long end = time.getTime();
            List<VmStaticsEntity> dataList = entityList.stream().filter(t -> t.getCreateTime().getTime() >= start && t.getCreateTime().getTime() < end).collect(Collectors.toList());

            OptionalDouble cpuUsageOptionalDouble = dataList.stream().mapToInt(VmStaticsEntity::getCpuUsage).average();
            OptionalDouble diskReadSpeedOptionalDouble = dataList.stream().mapToLong(VmStaticsEntity::getDiskReadSpeed).average();
            OptionalDouble diskWriteSpeedOptionalDouble = dataList.stream().mapToLong(VmStaticsEntity::getDiskWriteSpeed).average();
            OptionalDouble networkSendSpeedOptionalDouble = dataList.stream().mapToLong(VmStaticsEntity::getNetworkSendSpeed).average();
            OptionalDouble networkReceiveSpeedOptionalDouble = dataList.stream().mapToLong(VmStaticsEntity::getNetworkReceiveSpeed).average();
            if (cpuUsageOptionalDouble.isPresent()) {
                cpuUsage = (int) (cpuUsageOptionalDouble.getAsDouble());
            }
            if (diskReadSpeedOptionalDouble.isPresent()) {
                diskReadSpeed = (long) (diskReadSpeedOptionalDouble.getAsDouble());
            }
            if (diskWriteSpeedOptionalDouble.isPresent()) {
                diskWriteSpeed = (long) (diskWriteSpeedOptionalDouble.getAsDouble());
            }
            if (networkSendSpeedOptionalDouble.isPresent()) {
                networkSendSpeed = (long) (networkSendSpeedOptionalDouble.getAsDouble());
            }
            if (networkReceiveSpeedOptionalDouble.isPresent()) {
                networkReceiveSpeed = (long) (networkReceiveSpeedOptionalDouble.getAsDouble());
            }
            VmStatisticsInfo info = VmStatisticsInfo.builder()
                    .cpu(cpuUsage)
                    .read(diskReadSpeed)
                    .write(diskWriteSpeed)
                    .send(networkSendSpeed)
                    .receive(networkReceiveSpeed)
                    .time(starTime)
                    .build();
            list.add(info);
            starTime = time;
        }
        return list;

    }

    protected VmInfo initInstanceInfo(VmEntity vm) {
        VmInfo info = new VmInfo();
        info.setId(vm.getId());
        info.setClusterId(vm.getClusterId());
        info.setHostId(vm.getHostId());
        info.setCalculationSchemeId(vm.getCalculationSchemeId());
        info.setName(vm.getVmName());
        info.setIso(vm.getVmIso());
        info.setDescription(vm.getVmDescription());
        info.setIp(vm.getVmIp());
        info.setType(vm.getVmType());
        info.setStatus(vm.getVmStatus());
        info.setVncPort(vm.getVncPort());
        info.setCreateTime(vm.getCreateTime());
        info.setTemplateId(vm.getTemplateId());
        info.setGroupId(vm.getGroupId());
        return info;
    }
}
