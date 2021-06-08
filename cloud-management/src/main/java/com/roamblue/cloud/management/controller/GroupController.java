package com.roamblue.cloud.management.controller;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.annotation.Login;
import com.roamblue.cloud.management.bean.GroupInfo;
import com.roamblue.cloud.management.ui.GroupUiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "群组管理")
@Slf4j
public class GroupController {
    @Autowired
    private GroupUiService groupUiService;

    @Login
    @GetMapping("/management/group")
    @ApiOperation(value = "获取群组列表")
    public ResultUtil<List<GroupInfo>> listGroup() {
        return groupUiService.listGroup();
    }


    @Login
    @PostMapping("/management/group/create")
    @ApiOperation(value = "创建群组")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "群组名称")
    })
    public ResultUtil<GroupInfo> createGroup(@RequestParam("name") String name) {
        return groupUiService.createGroup(name);
    }

    @Login
    @PostMapping("/management/group/modify")
    @ApiOperation(value = "修改群组")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id"),
            @ApiImplicitParam(name = "name", value = "群组名称")
    })
    public ResultUtil<GroupInfo> modifyGroup(@RequestParam("id") int id, @RequestParam("name") String name) {

        return groupUiService.modifyGroup(id, name);
    }

    @Login
    @PostMapping("/management/group/destroy")
    @ApiOperation(value = "删除群组")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "群组ID")
    })
    public ResultUtil<Void> destroyGroupById(@RequestParam("id") int id) {
        return groupUiService.destroyGroupById(id);
    }
}
