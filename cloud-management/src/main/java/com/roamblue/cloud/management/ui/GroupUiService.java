package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.GroupInfo;

import java.util.List;

public interface GroupUiService {
    /**
     * 获取群组列表
     *
     * @return
     */
    ResultUtil<List<GroupInfo>> listGroup();

    /**
     * 创建群组
     *
     * @param name
     * @return
     */
    ResultUtil<GroupInfo> createGroup(String name);

    /**
     * 修改群组信息
     *
     * @param id
     * @param name
     * @return
     */
    ResultUtil<GroupInfo> modifyGroup(int id, String name);

    /**
     * 销毁群组
     *
     * @param id
     * @return
     */
    ResultUtil<Void> destroyGroupById(int id);
}
