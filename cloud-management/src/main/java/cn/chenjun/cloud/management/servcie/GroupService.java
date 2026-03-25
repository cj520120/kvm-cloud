package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GroupEntity;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author chenjun
 */
@Service
public class GroupService extends AbstractService {


    @Transactional(rollbackFor = Exception.class)
    public GroupEntity createGroup(String groupName) {
        GroupEntity entity = GroupEntity.builder().groupName(groupName).createTime(new Date()).build();
        groupDao.insert(entity);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(entity.getGroupId()).type(Constant.NotifyType.UPDATE_GROUP).build());
        return entity;
    }

    public Page<GroupEntity> search(String keyword, int no, int size) {
        Page<GroupEntity> page = this.groupDao.search(keyword, no, size);
        return page;
    }

    public GroupEntity getGroupById(Integer groupId) {
        GroupEntity entity = groupDao.findById(groupId);
        if (entity == null) {
            throw new CodeException(ErrorCode.GROUP_NOT_FOUND, "群组不存在");
        }
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public GroupEntity updateGroup(int groupId, String groupName) {
        GroupEntity entity = groupDao.findById(groupId);
        if (entity == null) {
            throw new CodeException(ErrorCode.GROUP_NOT_FOUND, "群组不存在");
        }
        entity.setGroupName(groupName);
        groupDao.update(entity);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(entity.getGroupId()).type(Constant.NotifyType.UPDATE_GROUP).build());
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)

    public void deleteGroup(int groupId) {
        groupDao.deleteById(groupId);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(groupId).type(Constant.NotifyType.UPDATE_GROUP).build());

    }

    public List<GroupEntity> listGroups() {
        List<GroupEntity> entities = groupDao.listAll();
        return entities;
    }


    public List<GroupEntity> listGroupByIds(List<Integer> groupIds) {
        return this.groupDao.listByIds(groupIds);
    }
}
