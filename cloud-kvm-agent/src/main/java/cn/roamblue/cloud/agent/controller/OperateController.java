package cn.roamblue.cloud.agent.controller;

import cn.roamblue.cloud.agent.operate.OperateDispatch;
import cn.roamblue.cloud.agent.operate.VolumeOperate;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeCloneRequest;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.bean.VolumeMigrateRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
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
import java.io.*;
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

    @PostMapping("/api/operate")
    public <T> ResultUtil<T> execute(@RequestParam("taskId") String taskId, @RequestParam("command") String command, @RequestParam("data") String data) {
        return dispatch.dispatch(taskId, command, data);
    }

    @PostMapping("/api/upload")
    public ResultUtil<VolumeInfo> uploadVolume(@RequestParam("name") String name,
                                               @RequestParam("path") String path,
                                               @RequestParam("storage") String storage,
                                               @RequestParam("volumeType") String volumeType,
                                               @RequestParam("volume") MultipartFile multipartFile) throws IOException {

        File file = new File(path + ".temp");
        try {
            file.createNewFile();
            try (InputStream inputStream = multipartFile.getInputStream()) {
                byte[] buffer = new byte[2048];
                int len = 0;
                try (OutputStream out = new FileOutputStream(file)) {
                    while ((len = inputStream.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
            }
            VolumeMigrateRequest request = VolumeMigrateRequest.builder()
                    .sourceStorage(storage)
                    .sourceVolume(file.getPath())
                    .targetStorage(storage)
                    .targetName(name)
                    .targetVolume(path)
                    .targetType(volumeType)
                    .build();
            return this.dispatch.dispatch(UUID.randomUUID().toString(), Constant.Command.VOLUME_MIGRATE, GsonBuilderUtil.create().toJson(request));
        } finally {
            file.delete();
        }
    }

    @GetMapping(value = "/api/download",
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public void downloadVolume(@RequestParam("name") String name,
                               @RequestParam("path") String path,
                               @RequestParam("storage") String storage,
                               @RequestParam("volumeType") String volumeType, HttpServletResponse response) throws IOException {
        String sub = "." + System.nanoTime();

        File file = new File(path + sub);
        try {
            file.createNewFile();
            VolumeCloneRequest request = VolumeCloneRequest.builder()
                    .sourceStorage(storage)
                    .sourceVolume(path)
                    .targetStorage(storage)
                    .targetName(name + sub)
                    .targetVolume(path + sub)
                    .targetType(volumeType)
                    .build();
            ResultUtil<VolumeInfo> resultUtil = this.dispatch.dispatch(UUID.randomUUID().toString(), Constant.Command.VOLUME_CLONE, GsonBuilderUtil.create().toJson(request));
            if (resultUtil.getCode() != ErrorCode.SUCCESS) {
                throw new CodeException(resultUtil.getCode(), resultUtil.getMessage());
            }
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name + "." + volumeType);
            @Cleanup
            InputStream in = new FileInputStream(file);
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
        } finally {
            file.deleteOnExit();
        }
    }
}
