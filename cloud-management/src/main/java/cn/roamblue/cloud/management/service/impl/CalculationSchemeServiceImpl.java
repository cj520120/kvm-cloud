package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.CalculationSchemeInfo;
import cn.roamblue.cloud.management.data.entity.CalculationSchemeEntity;
import cn.roamblue.cloud.management.data.mapper.CalculationSchemeMapper;
import cn.roamblue.cloud.management.data.mapper.VmMapper;
import cn.roamblue.cloud.management.service.CalculationSchemeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class CalculationSchemeServiceImpl extends AbstractService implements CalculationSchemeService {
    @Autowired
    private CalculationSchemeMapper calculationSchemeMapper;
    @Autowired
    private VmMapper vmMapper;

    @Override
    public List<CalculationSchemeInfo> listCalculationScheme() {
        List<CalculationSchemeEntity> entityList = calculationSchemeMapper.selectAll();
        List<CalculationSchemeInfo> list = entityList.stream().map(this::init).collect(Collectors.toList());
        list.add(0, this.getDefaultCalculationScheme());
        Collections.sort(list, Comparator.comparingInt(CalculationSchemeInfo::getCpu).thenComparingLong(CalculationSchemeInfo::getMemory).thenComparingInt(CalculationSchemeInfo::getSpeed));
        return list;
    }

    @Override
    public CalculationSchemeInfo findCalculationSchemeById(int id) {

        if (id <= 0) {
            return this.getDefaultCalculationScheme();
        }
        CalculationSchemeEntity entity = this.calculationSchemeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.CALCULATION_SCHEME_NOT_FOUND, localeMessage.getMessage("CALCULATION_SCHEME_NOT_FOUND", "计算方案不存在"));
        }
        return this.init(entity);
    }

    private CalculationSchemeInfo init(CalculationSchemeEntity entity) {
        return CalculationSchemeInfo.builder().id(entity.getId())
                .cpu(entity.getSchemeCpu())
                .memory(entity.getSchemeMemory())
                .name(entity.getSchemeName())
                .speed(entity.getSchemeCpuSpeed())
                .socket(entity.getSchemeCpuSocket())
                .core(entity.getSchemeCpuCore())
                .threads(entity.getSchemeCpuThreads())
                .createTime(entity.getCreateTime())
                .build();
    }

    @Override
    public CalculationSchemeInfo getDefaultCalculationScheme() {
        return CalculationSchemeInfo.builder().id(0)
                .cpu(1)
                .memory(512000L)
                .speed(1000)
                .socket(1)
                .core(1)
                .threads(1)
                .name("default")
                .createTime(new Date())
                .build();
    }

    @Override
    public CalculationSchemeInfo createCalculationScheme(String name, int cpu, int speed, long memory,int socket,int core,int threads) {

        CalculationSchemeEntity entity = CalculationSchemeEntity.builder()
                .schemeName(name)
                .schemeCpu(cpu)
                .schemeCpuSpeed(speed)
                .schemeCpuSocket(socket)
                .schemeCpuCore(core)
                .schemeCpuThreads(threads)
                .schemeMemory(memory * 1024)
                .createTime(new Date())
                .build();
        calculationSchemeMapper.insert(entity);
        return this.init(entity);
    }

    @Override
    public void destroyCalculationSchemeById(int id) {
        if (id == 0) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "系统方案禁止删除");
        }
        if (!vmMapper.findByCalculationSchemeId(id).isEmpty()) {
            throw new CodeException(ErrorCode.HAS_VM_ERROR, "该存储方案包含运行的实例");
        }
        calculationSchemeMapper.deleteById(id);

    }
}
