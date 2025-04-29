package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GroupInfoEntity;
import cn.chenjun.cloud.management.data.mapper.GroupMapper;
import cn.chenjun.cloud.management.model.GroupModel;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class GroupService extends AbstractService {
    @Autowired
    private GroupMapper mapper;

    public ResultUtil<GroupModel> createGroup(String groupName) {
        GroupInfoEntity entity = GroupInfoEntity.builder().groupName(groupName).createTime(new Date()).build();
        mapper.insert(entity);
        this.notifyService.publish(NotifyData.<Void>builder().id(entity.getGroupId()).type(Constant.NotifyType.UPDATE_GROUP).build());

        return ResultUtil.success(this.initGroup(entity));
    }

    public ResultUtil<Page<GroupModel>> search(String keyword, int no, int size) {
        QueryWrapper<GroupInfoEntity> wrapper = new QueryWrapper<>();
        if (!ObjectUtils.isEmpty(keyword)) {
            String condition = "%" + keyword + "%";
            wrapper.like(GroupInfoEntity.GROUP_NAME, condition);
        }
        int nCount = Math.toIntExact(this.mapper.selectCount(wrapper));
        int nOffset = (no - 1) * size;
        wrapper.last("limit " + nOffset + ", " + size);
        List<GroupInfoEntity> list = this.mapper.selectList(wrapper);
        List<GroupModel> models = list.stream().map(this::initGroup).collect(Collectors.toList());
        Page<GroupModel> page = Page.create(nCount, nOffset, size);
        page.setList(models);
        return ResultUtil.success(page);
    }
    public ResultUtil<GroupModel> getGroup(Integer groupId) {
        GroupInfoEntity entity = mapper.selectById(groupId);
        if (entity == null) {
            return ResultUtil.error(ErrorCode.GROUP_NOT_FOUND, "计算方案不存在");
        }
        return ResultUtil.success(this.initGroup(entity));
    }

    public ResultUtil<GroupModel> updateGroup(int groupId, String groupName) {
        GroupInfoEntity entity = mapper.selectById(groupId);
        if (entity == null) {
            throw new CodeException(ErrorCode.GROUP_NOT_FOUND, "计算方案不存在");
        }
        entity.setGroupName(groupName);
        mapper.updateById(entity);
        this.notifyService.publish(NotifyData.<Void>builder().id(entity.getGroupId()).type(Constant.NotifyType.UPDATE_GROUP).build());
        return ResultUtil.success(this.initGroup(entity));
    }

    public ResultUtil<Void> deleteGroup(int groupId) {
        mapper.deleteById(groupId);
        this.notifyService.publish(NotifyData.<Void>builder().id(groupId).type(Constant.NotifyType.UPDATE_GROUP).build());
        return ResultUtil.success();
    }

    public ResultUtil<List<GroupModel>> listGroups() {
        List<GroupInfoEntity> entities = mapper.selectList(null);
        return ResultUtil.success(entities.stream().map(this::initGroup).collect(Collectors.toList()));
    }


}
