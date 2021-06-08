package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.GroupInfo;

import java.util.List;

public interface GroupUiService {
    ResultUtil<List<GroupInfo>> listGroup();

    ResultUtil<GroupInfo> createGroup(String name);


    ResultUtil<GroupInfo> modifyGroup(int id, String name);

    ResultUtil<Void> destroyGroupById(int id);
}
