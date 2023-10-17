package cn.chenjun.cloud.agent.controller;

import cn.chenjun.cloud.agent.annotation.SignRequire;
import cn.chenjun.cloud.agent.operate.OperateDispatch;
import cn.chenjun.cloud.agent.operate.VolumeOperate;
import cn.chenjun.cloud.agent.util.ClientService;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeCloneRequest;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.bean.VolumeMigrateRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.UUID;

/**
 * @author chenjun
 */
@RestController
public class OperateController {
    @Autowired
    private OperateDispatch dispatch;

    @Autowired
    private VolumeOperate volumeOperate;
    @Autowired
    private ClientService clientService;


    @PostMapping("/api/init")
    public ResultUtil<Void> initHost(@RequestParam("managerUri") String managerUri,
                                     @RequestParam("clientId") String clientId,
                                     @RequestParam("clientSecret") String clientSecret) {
        return this.clientService.init(managerUri, clientId, clientSecret);
    }

    @SignRequire
    @PostMapping("/api/operate")
    public ResultUtil<?> execute(@RequestParam("taskId") String taskId, @RequestParam("command") String command, @RequestParam("data") String data) {
        return dispatch.dispatch(taskId, command, data);
    }

    @SignRequire(timeout = 86400000)
    @PostMapping("/api/upload")
    public ResultUtil<VolumeInfo> uploadVolume(@RequestParam("name") String name,
                                               @RequestParam("path") String path,
                                               @RequestParam("storage") String storage,
                                               @RequestParam("volumeType") String volumeType,
                                               @RequestParam("volume") MultipartFile multipartFile) throws IOException {

        String sub = "." + System.nanoTime();
        String tempPath = path + sub;
        File file = new File(tempPath);
        try {
            if (!file.createNewFile()) {
                return ResultUtil.error(ErrorCode.SERVER_ERROR, "创建文件失败");
            }
            multipartFile.transferTo(file);
            VolumeMigrateRequest request = VolumeMigrateRequest.builder()
                    .sourceStorage(storage)
                    .sourceVolume(tempPath)
                    .targetStorage(storage)
                    .targetName(name)
                    .targetVolume(path)
                    .targetType(volumeType)
                    .build();
            ResultUtil<?> resultUtil = this.dispatch.dispatch(UUID.randomUUID().toString(), Constant.Command.VOLUME_MIGRATE, GsonBuilderUtil.create().toJson(request));

            return ResultUtil.<VolumeInfo>builder().code(resultUtil.getCode()).message(resultUtil.getMessage()).data((VolumeInfo) resultUtil.getData()).build();
        } catch (Exception err) {
            return ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
        } finally {
            file.deleteOnExit();
        }
    }

    @SignRequire
    @GetMapping(value = "/api/download",
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public void downloadVolume(@RequestParam("name") String name,
                               @RequestParam("path") String path,
                               @RequestParam("storage") String storage,
                               @RequestParam("volumeType") String volumeType, HttpServletResponse response) throws IOException {
        String sub = "." + System.nanoTime();

        File file = new File(path + sub);
        try {
            if (!file.createNewFile()) {
                response.setStatus(HttpStatus.OK.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                String body = GsonBuilderUtil.create().toJson(ResultUtil.error(ErrorCode.SERVER_ERROR, "创建文件失败"));
                @Cleanup
                OutputStream outputStream = response.getOutputStream();
                outputStream.write(body.getBytes());
                return;
            }
            VolumeCloneRequest request = VolumeCloneRequest.builder()
                    .sourceStorage(storage)
                    .sourceVolume(path)
                    .targetStorage(storage)
                    .targetName(name + sub)
                    .targetVolume(path + sub)
                    .targetType(volumeType)
                    .build();
            ResultUtil<?> resultUtil = this.dispatch.dispatch(UUID.randomUUID().toString(), Constant.Command.VOLUME_CLONE, GsonBuilderUtil.create().toJson(request));
            if (resultUtil.getCode() != ErrorCode.SUCCESS) {
                throw new CodeException(resultUtil.getCode(), resultUtil.getMessage());
            }
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name + "." + volumeType);
            @Cleanup
            InputStream in = Files.newInputStream(file.toPath());
            response.setContentLengthLong(file.length());
            @Cleanup
            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[2048];
            int len = 0;
            while ((len = in.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, len);
            }

        } catch (CodeException err) {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String body = GsonBuilderUtil.create().toJson(ResultUtil.error(err.getCode(), err.getMessage()));
            @Cleanup
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(body.getBytes());
        } catch (Exception err) {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String body = GsonBuilderUtil.create().toJson(ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage()));
            @Cleanup
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(body.getBytes());
        } finally {
            file.deleteOnExit();
        }
    }
}
