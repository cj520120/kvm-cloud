package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.management.data.entity.GroupEntity;
import cn.chenjun.cloud.management.model.GroupModel;
import cn.chenjun.cloud.management.servcie.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        List<GroupEntity> entityList = this.groupService.listGroups();
        List<GroupModel> modelList = entityList.stream().map(this.convertService::initGroupModel).collect(Collectors.toList());
        return ResultUtil.success(modelList);
    }

    @GetMapping("/api/group/search")
    public ResultUtil<Page<GroupModel>> search(@RequestParam(value = "keyword", required = false) String keyword,
                                               @RequestParam("no") int no,
                                               @RequestParam("size") int size) {
        Page<GroupEntity> pageEntity = this.groupService.search(keyword, no, size);
        Page<GroupModel> pageModel = Page.convert(pageEntity, this.convertService::initGroupModel);
        return ResultUtil.success(pageModel);
    }

    @GetMapping("/api/group/info")
    public ResultUtil<GroupModel> getGroupInfo(@RequestParam("groupId") int groupId) {
        GroupEntity entity = this.groupService.getGroupById(groupId);
        return ResultUtil.success(this.convertService.initGroupModel(entity));
    }


    @PutMapping("/api/group/create")
    public ResultUtil<GroupModel> createGroup(@RequestParam("groupName") String groupName) {
        GroupEntity entity = this.lockRun(() -> this.groupService.createGroup(groupName));
        return ResultUtil.success(this.convertService.initGroupModel(entity));
    }


    @PostMapping("/api/group/update")
    public ResultUtil<GroupModel> updateGroup(@RequestParam("groupId") int groupId, @RequestParam("groupName") String groupName) {
        GroupEntity entity = this.lockRun(() -> this.groupService.updateGroup(groupId, groupName));
        return ResultUtil.success(this.convertService.initGroupModel(entity));
    }


    @DeleteMapping("/api/group/destroy")
    public ResultUtil<Void> deleteGroup(@RequestParam("groupId") int groupId) {
        this.lockRun(() -> this.groupService.deleteGroup(groupId));
        return ResultUtil.success();
    }
}
