package com.roamblue.cloud.management.service.impl;

import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.bean.GroupInfo;
import com.roamblue.cloud.management.data.entity.GroupEntity;
import com.roamblue.cloud.management.data.mapper.GroupMapper;
import com.roamblue.cloud.management.service.GroupService;
import com.roamblue.cloud.management.util.BeanConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {
    @Autowired
    private GroupMapper mapper;

    @Override
    public List<GroupInfo> listGroup() {
        return BeanConverter.convert(mapper.selectAll(), this::init);
    }

    @Override
    public GroupInfo createGroup(String name) {
        GroupEntity entity = GroupEntity.builder().groupName(name).createTime(new Date()).build();
        mapper.insert(entity);
        return this.init(entity);
    }

    @Override
    public GroupInfo modifyGroup(int id, String name) {
        GroupEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.GROUP_NOT_FOUND, "群组不存在");
        }
        entity.setGroupName(name);
        mapper.updateById(entity);
        return this.init(entity);
    }

    @Override
    public void destroyGroupById(int id) {
        this.mapper.deleteById(id);
    }

    private GroupInfo init(GroupEntity entity) {
        GroupInfo model = GroupInfo.builder().id(entity.getId()).name(entity.getGroupName()).createTime(entity.getCreateTime()).build();
        return model;
    }
}
