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
    public ResultUtil<SchemeModel> createScheme(String name, int cpu, long memory, int speed, int sockets, int cores, int threads) {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入架构名称");
        }
        if (cpu <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入CPU");
        }
        if (memory <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入内存");
        }
        if (speed < 0) {
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
        SchemeEntity entity = SchemeEntity.builder().name(name).cpu(cpu).memory(memory).speed(speed).sockets(sockets).cores(cores).threads(threads).build();
        this.schemeMapper.insert(entity);
        this.eventService.publish(NotifyData.<Void>builder().id(entity.getSchemeId()).type(Constant.NotifyType.UPDATE_SCHEME).build());

        return ResultUtil.success(this.initScheme(entity));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SchemeModel> updateScheme(int schemeId, String name, int cpu, long memory, int speed, int sockets, int cores, int threads) {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入架构名称");
        }
        if (cpu <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入CPU");
        }
        if (memory <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入内存");
        }
        if (speed < 0) {
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
        SchemeEntity entity = this.schemeMapper.selectById(schemeId);
        if (entity == null) {
            throw new CodeException(ErrorCode.SCHEME_NOT_FOUND, "计算方案不存在");
        }

        entity.setName(name);
        entity.setCpu(cpu);
        entity.setMemory(memory);
        entity.setSpeed(speed);
        entity.setSockets(sockets);
        entity.setCores(cores);
        entity.setThreads(threads);
        this.schemeMapper.updateById(entity);
        this.eventService.publish(NotifyData.<Void>builder().id(entity.getSchemeId()).type(Constant.NotifyType.UPDATE_SCHEME).build());
        return ResultUtil.success(this.initScheme(entity));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> destroyScheme(int schemeId) {
        this.schemeMapper.deleteById(schemeId);
        this.eventService.publish(NotifyData.<Void>builder().id(schemeId).type(Constant.NotifyType.UPDATE_SCHEME).build());
        return ResultUtil.success();
    }
}
