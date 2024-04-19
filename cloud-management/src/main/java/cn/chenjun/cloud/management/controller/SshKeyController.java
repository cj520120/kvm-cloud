package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.model.CreateSshAuthorizedModel;
import cn.chenjun.cloud.management.model.SshAuthorizedModel;
import cn.chenjun.cloud.management.servcie.SshAuthorizedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenjun
 */
@LoginRequire
@RestController
public class SshKeyController extends BaseController {
    @Autowired
    private SshAuthorizedService sshAuthorizedService;

    @GetMapping("/api/ssh/all")
    public ResultUtil<List<SshAuthorizedModel>> listAllSshKeys() {
        return this.lockRun(() -> this.sshAuthorizedService.listAllSshKeys());
    }

    @GetMapping("/api/ssh/info")
    public ResultUtil<SshAuthorizedModel> getSshKey(@RequestParam("id") int id) {
        return this.lockRun(() -> this.sshAuthorizedService.getSshKey(id));
    }

    @PutMapping("/api/ssh/import")
    public ResultUtil<SshAuthorizedModel> importSshKey(@RequestParam("name") String name, @RequestParam("key") String key) {
        return this.lockRun(() -> this.sshAuthorizedService.importSshKey(name, key));
    }

    @PutMapping("/api/ssh/create")
    public ResultUtil<CreateSshAuthorizedModel> createKey(@RequestParam("name") String name) {
        return this.lockRun(() -> this.sshAuthorizedService.createSshKey(name));
    }


    @DeleteMapping("/api/ssh/destroy")
    public ResultUtil<Void> deleteSshKey(@RequestParam("id") int id) {
        return this.lockRun(() -> this.sshAuthorizedService.deleteSshKey(id));
    }
}
