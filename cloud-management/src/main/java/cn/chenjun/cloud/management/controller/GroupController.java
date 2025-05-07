package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.management.model.GroupModel;
import cn.chenjun.cloud.management.servcie.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenjun
 */
@LoginRequire
@RestController
public class GroupController extends BaseController {
    @Autowired
    private GroupService groupService;

    @GetMapping("/api/group/all")
    public ResultUtil<List<GroupModel>> listGroups() {
        return this.lockRun(() -> this.groupService.listGroups());
    }

    @GetMapping("/api/group/search")
    public ResultUtil<Page<GroupModel>> search(@RequestParam(value = "keyword",required = false) String keyword,
                                               @RequestParam("no") int no,
                                               @RequestParam("size") int size) {
        return this.lockRun(() -> this.groupService.search(keyword, no, size));
    }
    @GetMapping("/api/group/info")
    public ResultUtil<GroupModel> getGroupInfo(@RequestParam("groupId") int groupId) {
        return this.lockRun(() -> this.groupService.getGroup(groupId));
    }

    
    @PutMapping("/api/group/create")
    public ResultUtil<GroupModel> createGroup(@RequestParam("groupName") String groupName) {
        return this.lockRun(() -> this.groupService.createGroup(groupName));
    }

    
    @PostMapping("/api/group/update")
    public ResultUtil<GroupModel> updateGroup(@RequestParam("groupId") int groupId, @RequestParam("groupName") String groupName) {
        return this.lockRun(() -> this.groupService.updateGroup(groupId, groupName));
    }

    
    @DeleteMapping("/api/group/destroy")
    public ResultUtil<Void> deleteGroup(@RequestParam("groupId") int groupId) {
        return this.lockRun(() -> this.groupService.deleteGroup(groupId));
    }
}
