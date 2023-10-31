package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.model.GroupModel;
import cn.chenjun.cloud.management.servcie.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@LoginRequire
@RestController
public class GroupController {
    @Autowired
    private GroupService groupService;

    @GetMapping("/api/group/all")
    public ResultUtil<List<GroupModel>> listGroups() {
        return this.groupService.listGroups();
    }

    @GetMapping("/api/group/info")
    public ResultUtil<GroupModel> getGroupInfo(@RequestParam("groupId") int groupId) {
        return this.groupService.getGroup(groupId);
    }

    @PutMapping("/api/group/create")
    public ResultUtil<GroupModel> createGroup(@RequestParam("groupName") String groupName) {
        return this.groupService.createGroup(groupName);
    }

    @PostMapping("/api/group/update")
    public ResultUtil<GroupModel> updateGroup(@RequestParam("groupId") int groupId, @RequestParam("groupName") String groupName) {
        return this.groupService.updateGroup(groupId, groupName);
    }

    @DeleteMapping("/api/group/destroy")
    public ResultUtil<Void> deleteGroup(@RequestParam("groupId") int groupId) {
        return this.groupService.deleteGroup(groupId);
    }
}
