package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.bean.GroupInfo;
import cn.roamblue.cloud.management.ui.GroupUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 群组管理
 *
 * @author chenjun
 */
@RestController
public class GroupController {
    @Autowired
    private GroupUiService groupUiService;

    /**
     * 获取群组列表
     *
     * @return
     */
    @Login
    @GetMapping("/management/group")
    public ResultUtil<List<GroupInfo>> listGroup() {
        return groupUiService.listGroup();
    }

    /**
     * 创建群组
     *
     * @param name 群组名称
     * @return
     */
    @Login
    @PostMapping("/management/group/create")
    public ResultUtil<GroupInfo> createGroup(@RequestParam("name") String name) {
        return groupUiService.createGroup(name);
    }

    /**
     * 修改群组
     *
     * @param id   id
     * @param name 群组名称
     * @return
     */
    @Login
    @PostMapping("/management/group/modify")
    public ResultUtil<GroupInfo> modifyGroup(@RequestParam("id") int id, @RequestParam("name") String name) {

        return groupUiService.modifyGroup(id, name);
    }

    /**
     * 删除群组
     *
     * @param id 群组ID
     * @return
     */
    @Login
    @PostMapping("/management/group/destroy")
    public ResultUtil<Void> destroyGroupById(@RequestParam("id") int id) {
        return groupUiService.destroyGroupById(id);
    }
}
