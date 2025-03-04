package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.SchemeEntity;
import cn.chenjun.cloud.management.model.SchemeModel;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class SchemeService extends AbstractService {


    private static void verifySchemeParam(String name, int cpu, long memory, int share, int sockets, int cores, int threads) {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入架构名称");
        }
        if (cpu <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入CPU");
        }
        if (memory <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入内存");
        }
        if (share < 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的配额");
        }
        if (sockets < 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的Sockets");
        }
        if (cores < 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的Cores");
        }
        if (threads < 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的Threads");
        }
        int coreCpu = sockets * cores * threads;
        if (coreCpu != 0 && cpu != coreCpu) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "Cpu架构设置不正确,sockets、core、threads 参数相乘需要等于Cpu数量");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SchemeModel> getSchemeInfo(int schemeId) {
        SchemeEntity entity = this.schemeMapper.selectById(schemeId);
        if (entity == null) {
            return ResultUtil.error(ErrorCode.SCHEME_NOT_FOUND, "计算方案不存在");
        }
        return ResultUtil.success(this.initScheme(entity));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<SchemeModel>> listScheme() {
        List<SchemeEntity> list = this.schemeMapper.selectList(new QueryWrapper<>());
        List<SchemeModel> models = list.stream().map(this::initScheme).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SchemeModel> createScheme(String name, int cpu, long memory, int share, int sockets, int cores, int threads) {
        verifySchemeParam(name, cpu, memory, share, sockets, cores, threads);
        SchemeEntity entity = SchemeEntity.builder().name(name).cpu(cpu).memory(memory).share(share).sockets(sockets).cores(cores).threads(threads).build();
        this.schemeMapper.insert(entity);
        this.notifyService.publish(NotifyData.<Void>builder().id(entity.getSchemeId()).type(Constant.NotifyType.UPDATE_SCHEME).build());

        return ResultUtil.success(this.initScheme(entity));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SchemeModel> updateScheme(int schemeId, String name, int cpu, long memory, int share, int sockets, int cores, int threads) {
        verifySchemeParam(name, cpu, memory, share, sockets, cores, threads);
        SchemeEntity entity = this.schemeMapper.selectById(schemeId);
        if (entity == null) {
            throw new CodeException(ErrorCode.SCHEME_NOT_FOUND, "计算方案不存在");
        }
        entity.setName(name);
        entity.setCpu(cpu);
        entity.setMemory(memory);
        entity.setShare(share);
        entity.setSockets(sockets);
        entity.setCores(cores);
        entity.setThreads(threads);
        this.schemeMapper.updateById(entity);
        this.notifyService.publish(NotifyData.<Void>builder().id(entity.getSchemeId()).type(Constant.NotifyType.UPDATE_SCHEME).build());
        return ResultUtil.success(this.initScheme(entity));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> destroyScheme(int schemeId) {
        this.schemeMapper.deleteById(schemeId);
        this.notifyService.publish(NotifyData.<Void>builder().id(schemeId).type(Constant.NotifyType.UPDATE_SCHEME).build());
        return ResultUtil.success();
    }
}
