package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.annotation.PermissionRequire;
import cn.chenjun.cloud.management.model.CreateSshAuthorizedModel;
import cn.chenjun.cloud.management.model.SshAuthorizedModel;
import cn.chenjun.cloud.management.servcie.SshAuthorizedService;
import cn.chenjun.cloud.management.servcie.bean.MemSshInfo;
import cn.chenjun.cloud.management.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

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
        return this.lockRun(() -> this.sshAuthorizedService.listAllSshKeys());
    }

    @LoginRequire
    @GetMapping("/api/ssh/search")
    public ResultUtil<Page<SshAuthorizedModel>> search(@RequestParam(value = "keyword",required = false) String keyword,
                                                       @RequestParam("no") int no,
                                                       @RequestParam("size") int size) {
        return this.lockRun(() -> this.sshAuthorizedService.search(keyword, no, size));
    }
    @LoginRequire
    @GetMapping("/api/ssh/info")
    public ResultUtil<SshAuthorizedModel> getSshKey(@RequestParam("id") int id) {
        return this.lockRun(() -> this.sshAuthorizedService.getSshKey(id));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @LoginRequire
    @PutMapping("/api/ssh/import")
    public ResultUtil<SshAuthorizedModel> importSshKey(@RequestParam("name") String name, @RequestParam("publicKey") String publicKey, @RequestParam("privateKey") String privateKey) {
        return this.lockRun(() -> this.sshAuthorizedService.importSshKey(name, publicKey, privateKey));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @LoginRequire
    @PutMapping("/api/ssh/create")
    public ResultUtil<CreateSshAuthorizedModel> createKey(@RequestParam("name") String name) {
        return this.lockRun(() -> this.sshAuthorizedService.createSshKey(name));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @LoginRequire
    @PostMapping("/api/ssh/modify")
    public ResultUtil<SshAuthorizedModel> modify(@RequestParam("id") int id, @RequestParam("name") String name) {
        return this.lockRun(() -> this.sshAuthorizedService.modifySshKey(id, name));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @LoginRequire
    @DeleteMapping("/api/ssh/destroy")
    public ResultUtil<Void> deleteSshKey(@RequestParam("id") int id) {
        return this.lockRun(() -> this.sshAuthorizedService.deleteSshKey(id));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @LoginRequire
    @PostMapping("/api/ssh/download/key")
    public ResultUtil<String> createDownloadKey(@RequestParam("id") int id) {
        return this.lockRun(() -> this.sshAuthorizedService.createDownloadKey(id));
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
