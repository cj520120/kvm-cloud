package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.SchemeEntity;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

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

    public SchemeEntity getSchemeInfo(int schemeId) {
        SchemeEntity entity = this.schemeDao.findById(schemeId);
        if (entity == null) {
            throw new CodeException(ErrorCode.SCHEME_NOT_FOUND, "计算方案不存在");
        }
        return entity;
    }

    public List<SchemeEntity> listScheme() {
        List<SchemeEntity> list = this.schemeDao.listAll();
        return list;
    }

    public Page<SchemeEntity> search(String keyword, int no, int size) {
        Page<SchemeEntity> page = this.schemeDao.search(keyword, no, size);
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    public SchemeEntity createScheme(String name, int cpu, long memory, int share, int sockets, int cores, int threads) {
        verifySchemeParam(name, cpu, memory, share, sockets, cores, threads);
        SchemeEntity entity = SchemeEntity.builder().name(name).cpu(cpu).memory(memory).share(share).sockets(sockets).cores(cores).threads(threads).build();
        this.schemeDao.insert(entity);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(entity.getSchemeId()).type(Constant.NotifyType.UPDATE_SCHEME).build());

        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public SchemeEntity updateScheme(int schemeId, String name, int cpu, long memory, int share, int sockets, int cores, int threads) {
        verifySchemeParam(name, cpu, memory, share, sockets, cores, threads);
        SchemeEntity entity = this.schemeDao.findById(schemeId);
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
        this.schemeDao.update(entity);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(entity.getSchemeId()).type(Constant.NotifyType.UPDATE_SCHEME).build());
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public void destroyScheme(int schemeId) {
        this.schemeDao.deleteById(schemeId);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(schemeId).type(Constant.NotifyType.UPDATE_SCHEME).build());

    }

    public List<SchemeEntity> listSchemeByIds(List<Integer> schemeIds) {
        return this.schemeDao.listByIds(schemeIds);
    }
}
