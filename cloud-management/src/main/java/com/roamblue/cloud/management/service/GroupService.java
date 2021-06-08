package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.GroupInfo;

import java.util.List;

public interface GroupService {
    /**
     * @return
     */
    List<GroupInfo> listGroup();


    /**
     * 创建
     *
     * @return
     */
    GroupInfo createGroup(String name);

    /**
     * 创建
     *
     * @return
     */
    GroupInfo modifyGroup(int id, String name);

    /**
     * 销毁
     *
     * @return
     */
    void destroyGroupById(int id);

}
