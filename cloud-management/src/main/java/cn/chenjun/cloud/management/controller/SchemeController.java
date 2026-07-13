package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.SchemeEntity;
import cn.chenjun.cloud.management.model.SchemeCreateRequest;
import cn.chenjun.cloud.management.model.SchemeDestroyRequest;
import cn.chenjun.cloud.management.model.SchemeModel;
import cn.chenjun.cloud.management.model.SchemeModifyRequest;
import cn.chenjun.cloud.management.servcie.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@LoginRequire
@RestController
public class SchemeController extends BaseController {
    @Autowired
    private SchemeService schemeService;

    @GetMapping("/api/scheme/info")
    public ResultUtil<SchemeModel> getSchemeInfo(@RequestParam("schemeId") int schemeId) {
        SchemeEntity scheme = this.schemeService.getSchemeInfo(schemeId);
        return ResultUtil.success(this.convertService.initSchemeModel(scheme));
    }

    @GetMapping("/api/scheme/all")
    public ResultUtil<List<SchemeModel>> listScheme() {

        List<SchemeEntity> schemes = this.schemeService.listScheme();
        List<SchemeModel> models = schemes.stream().map(this.convertService::initSchemeModel).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @GetMapping("/api/scheme/search")
    public ResultUtil<Page<SchemeModel>> search(@RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam("no") int no,
                                                @RequestParam("size") int size) {
        Page<SchemeEntity> page = this.schemeService.search(keyword, no, size);
        Page<SchemeModel> models = Page.convert(page, this.convertService::initSchemeModel);
        return ResultUtil.success(models);
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PutMapping("/api/scheme/create")
    public ResultUtil<SchemeModel> createScheme(@RequestBody SchemeCreateRequest request) {
        request.validate();
        SchemeEntity scheme = this.globalLockCall(() -> this.schemeService.createScheme(request.getName(), request.getCpu(), request.getMemory() * 1024, request.getShare(), request.getSockets(), request.getCores(), request.getThreads()));
        return ResultUtil.success(this.convertService.initSchemeModel(scheme));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/scheme/modify")
    public ResultUtil<SchemeModel> updateScheme(@RequestBody SchemeModifyRequest request) {
        request.validate();
        SchemeEntity scheme = this.globalLockCall(() -> this.schemeService.updateScheme(request.getSchemeId(), request.getName(), request.getCpu(), request.getMemory() * 1024, request.getShare(), request.getSockets(), request.getCores(), request.getThreads()));
        return ResultUtil.success(this.convertService.initSchemeModel(scheme));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @DeleteMapping("/api/scheme/destroy")
    public ResultUtil<Void> destroyScheme(@RequestBody SchemeDestroyRequest request) {
        request.validate();
        this.globalLockCall(() -> this.schemeService.destroyScheme(request.getSchemeId()));
        return ResultUtil.success();
    }
}
