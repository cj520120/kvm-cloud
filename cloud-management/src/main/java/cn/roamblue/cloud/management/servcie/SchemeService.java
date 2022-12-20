package cn.roamblue.cloud.management.servcie;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.SchemeEntity;
import cn.roamblue.cloud.management.model.SchemeModel;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchemeService extends AbstractService {


    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SchemeModel> getSchemeInfo(int schemeId) {
        SchemeEntity entity = this.schemeMapper.selectById(schemeId);
        if (entity == null) {
            throw new CodeException(ErrorCode.SCHEME_NOT_FOUND, "计算方案不存在");
        }
        return ResultUtil.success(this.initScheme(entity));
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<SchemeModel>> listScheme() {
        List<SchemeEntity> list = this.schemeMapper.selectList(new QueryWrapper<>());
        List<SchemeModel> models = list.stream().map(this::initScheme).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SchemeModel> createScheme(String name, int cpu, long memory, int speed, int sockets, int cores, int threads) {
        SchemeEntity entity = SchemeEntity.builder().name(name).cpu(cpu).memory(memory).speed(speed).sockets(sockets).cores(cores).threads(threads).build();
        this.schemeMapper.insert(entity);

        return ResultUtil.success(this.initScheme(entity));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SchemeModel> updateScheme(int schemeId, String name, int cpu, long memory, int speed, int sockets, int cores, int threads) {
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
        return ResultUtil.success(this.initScheme(entity));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> destroyScheme(int schemeId) {
        this.schemeMapper.deleteById(schemeId);
        return ResultUtil.success();
    }
}
