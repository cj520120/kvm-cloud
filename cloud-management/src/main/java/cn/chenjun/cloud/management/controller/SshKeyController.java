package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.SshAuthorizedEntity;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.servcie.SshAuthorizedService;
import cn.chenjun.cloud.management.servcie.bean.MemSshInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */

@RestController
public class SshKeyController extends BaseController {
    @Autowired
    private SshAuthorizedService sshAuthorizedService;

    @LoginRequire
    @GetMapping("/api/ssh/all")
    public ResultUtil<List<SshAuthorizedModel>> listAllSshKeys() {
        List<SshAuthorizedEntity> sshs = this.sshAuthorizedService.listAllSshKeys();

        List<SshAuthorizedModel> models = sshs.stream().map(this.convertService::initSshModel).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @LoginRequire
    @GetMapping("/api/ssh/search")
    public ResultUtil<Page<SshAuthorizedModel>> search(@RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam("no") int no,
                                                       @RequestParam("size") int size) {
        Page<SshAuthorizedEntity> page = this.sshAuthorizedService.search(keyword, no, size);
        Page<SshAuthorizedModel> pageModels = Page.convert(page, this.convertService::initSshModel);
        return ResultUtil.success(pageModels);
    }

    @LoginRequire
    @GetMapping("/api/ssh/info")
    public ResultUtil<SshAuthorizedModel> getSshKey(@RequestParam("id") int id) {
        SshAuthorizedEntity ssh = this.sshAuthorizedService.getSshKey(id);
        return ResultUtil.success(convertService.initSshModel(ssh));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @LoginRequire
    @PutMapping("/api/ssh/import")
    public ResultUtil<SshAuthorizedModel> importSshKey(@RequestBody SshImportRequest request) {
        request.validate();
        SshAuthorizedEntity ssh = this.globalLockCall(() -> this.sshAuthorizedService.importSshKey(request.getName(), request.getPublicKey(), request.getPrivateKey()));
        return ResultUtil.success(convertService.initSshModel(ssh));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @LoginRequire
    @PutMapping("/api/ssh/create")
    public ResultUtil<CreateSshAuthorizedModel> createKey(@RequestBody SshCreateRequest request) {
        request.validate();
        SshAuthorizedEntity ssh = this.globalLockCall(() -> this.sshAuthorizedService.createSshKey(request.getName()));
        CreateSshAuthorizedModel model = CreateSshAuthorizedModel.builder().id(ssh.getId()).name(request.getName()).publicKey(ssh.getSshPublicKey()).privateKey(ssh.getSshPrivateKey()).build();
        return ResultUtil.success(model);
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @LoginRequire
    @PostMapping("/api/ssh/modify")
    public ResultUtil<SshAuthorizedModel> modify(@RequestBody SshModifyRequest request) {
        request.validate();
        SshAuthorizedEntity ssh = this.globalLockCall(() -> this.sshAuthorizedService.modifySshKey(request.getId(), request.getName()));
        return ResultUtil.success(convertService.initSshModel(ssh));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @LoginRequire
    @DeleteMapping("/api/ssh/destroy")
    public ResultUtil<Void> deleteSshKey(@RequestBody SshDestroyRequest request) {
        request.validate();
        this.globalLockCall(() -> this.sshAuthorizedService.deleteSshKey(request.getId()));
        return ResultUtil.success();
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @LoginRequire
    @PostMapping("/api/ssh/download/key")
    public ResultUtil<String> createDownloadKey(@RequestBody SshDownloadRequest request) {
        request.validate();
        String token = this.sshAuthorizedService.createDownloadKey(request.getId());
        return ResultUtil.success(token);
    }

    @GetMapping("/api/ssh/download")
    public ResponseEntity<String> downloadSshPrivateKey(@RequestParam("token") String token) throws UnsupportedEncodingException {
        MemSshInfo memSshInfo = this.sshAuthorizedService.getDownloadKey(token);
        HttpHeaders headers = new HttpHeaders();
        if (memSshInfo != null) {
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(memSshInfo.getName(), "utf-8") + "-" + memSshInfo.getId() + ".pem");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<>(memSshInfo.getPrivateKey(), headers, HttpStatus.OK);
        } else {
            headers.setContentType(MediaType.TEXT_HTML);
            return new ResponseEntity<>(headers, HttpStatus.NOT_FOUND);
        }

    }
}
