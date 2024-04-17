package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.SshAuthorizedEntity;
import cn.chenjun.cloud.management.model.SshAuthorizedModel;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class SshAuthorizedService extends AbstractService {

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<SshAuthorizedModel>> listAllSshKeys() {
        List<SshAuthorizedEntity> list = this.sshAuthorizedMapper.selectList(new QueryWrapper<>());
        List<SshAuthorizedModel> models = list.stream().map(this::initSshAuthorized).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SshAuthorizedModel> getSshKey(int id) {
        SshAuthorizedEntity entity = this.sshAuthorizedMapper.selectById(id);
        if (entity == null) {
            return ResultUtil.error(ErrorCode.SSH_AUTHORIZED_NOT_FOUND, "SSH公钥不存在");
        }
        return ResultUtil.success(this.initSshAuthorized(entity));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<SshAuthorizedModel> createSshKey(String name, String key) {
        SshAuthorizedEntity entity = SshAuthorizedEntity.builder().sshName(name).sshKey(key).build();
        this.sshAuthorizedMapper.insert(entity);
        this.eventService.publish(NotifyData.<Void>builder().id(entity.getId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_SSH_KEY).build());

        return ResultUtil.success(this.initSshAuthorized(entity));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> deleteSshKey(int id) {
        this.sshAuthorizedMapper.deleteById(id);
        this.eventService.publish(NotifyData.<Void>builder().id(id).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_SSH_KEY).build());
        return ResultUtil.success();
    }
}
