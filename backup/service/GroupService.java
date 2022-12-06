package cn.roamblue.cloud.management.service;

import cn.roamblue.cloud.management.bean.GroupInfo;

import java.util.List;

/**
 * @author chenjun
 */
public interface GroupService {
    /**
     * 获取群组列表
     *
     * @return
     */
    List<GroupInfo> listGroup();


    /**
     * 创建
     *
     * @param name
     * @return
     */
    GroupInfo createGroup(String name);

    /**
     * 修改
     *
     * @param id
     * @param name
     * @return
     */
    GroupInfo modifyGroup(int id, String name);

    /**
     * 销毁
     *
     * @param id
     * @return
     */
    void destroyGroupById(int id);

}
