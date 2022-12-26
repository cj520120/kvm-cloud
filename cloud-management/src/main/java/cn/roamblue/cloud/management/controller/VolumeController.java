package cn.roamblue.cloud.management.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.model.*;
import cn.roamblue.cloud.management.servcie.VolumeService;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class VolumeController {

    @Autowired
    private VolumeService volumeService;

    @GetMapping("/api/volume/all")
    public ResultUtil<List<VolumeModel>> listVolumes() {
        return this.volumeService.listVolumes();
    }
    @GetMapping("/api/volume/not/attach/all")
    public ResultUtil<List<VolumeModel>> listNoAttachVolumes() {
        return this.volumeService.listNoAttachVolumes();
    }
    @GetMapping("/api/volume/info")
    public ResultUtil<VolumeModel> getVolumeInfo(@RequestParam("volumeId") int volumeId) {
        return this.volumeService.getVolumeInfo(volumeId);
    }

    @PutMapping("/api/volume/create")
    public ResultUtil<VolumeModel> createVolume(@RequestParam("description") String description,
                                                @RequestParam("storageId") int storageId,
                                                @RequestParam("volumeType") String volumeType,
                                                @RequestParam("volumeSize") long volumeSize) {
        return this.volumeService.createVolume(description, storageId, 0, 0, volumeType, volumeSize * 1024 * 1024 * 1024);
    }

    @PostMapping("/api/volume/upload")
    public ResultUtil<VolumeModel> uploadVolume(@RequestParam("description") String description,
                                                @RequestParam("storageId") int storageId,
                                                @RequestParam("volumeType") String volumeType,
                                                @RequestParam("volume") MultipartFile multipartFile) throws IOException {
        File parentPath = FileUtil.mkdir("./upload");
        File file = new File(parentPath.getPath() + "/" + UUID.randomUUID());
        file.createNewFile();
        try {
            try (InputStream inputStream = multipartFile.getInputStream()) {
                byte[] buffer = new byte[2048];
                int len = 0;
                try (OutputStream out = new FileOutputStream(file)) {
                    while ((len = inputStream.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
            }
            return this.volumeService.uploadVolume(description, storageId, volumeType, file);
        } finally {
            file.deleteOnExit();
        }
    }

    @GetMapping(value = "/api/volume/download",
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public void downloadVolume(@RequestParam("volumeId") int volumeId,@RequestParam("volumeType") String volumeType,HttpServletResponse response) throws IOException {
        try {
            ResultUtil<DownloadModel> resultUtil = this.volumeService.getDownloadUri(volumeId);
            DownloadModel model=resultUtil.getData();
            String uri= String.format("%s/api/download?storage=%s&name=%s&path=%s&volumeType=%s",model.getHost(),URLEncoder.encode(model.getStorage(),"utf-8"),URLEncoder.encode(model.getName(),"utf-8"),URLEncoder.encode(model.getPath(),"utf-8"),URLEncoder.encode(volumeType,"utf-8"));
            URL url = new URL(uri);
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + model.getName() + "." + volumeType);
            URLConnection conn = url.openConnection();
            response.setContentLengthLong(conn.getContentLengthLong());
            @Cleanup
            InputStream in = conn.getInputStream();

            @Cleanup
            OutputStream outputStream=response.getOutputStream();
            byte[] buffer = new byte[2048];
            int len = 0;
            while ((len = in.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, len);
            }
        } catch (CodeException err){
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String body= GsonBuilderUtil.create().toJson(ResultUtil.error(err.getCode(),err.getMessage()));
            @Cleanup
            OutputStream outputStream=response.getOutputStream();
            outputStream.write(body.getBytes());
        }catch (Exception err) {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String body=GsonBuilderUtil.create().toJson(ResultUtil.error(ErrorCode.SERVER_ERROR,err.getMessage()));
            @Cleanup
            OutputStream outputStream=response.getOutputStream();
            outputStream.write(body.getBytes());
        }

    }

    @PutMapping("/api/volume/clone")
    public ResultUtil<CloneModel> cloneVolume(@RequestParam("description") String description,
                                              @RequestParam("sourceVolumeId") int sourceVolumeId,
                                              @RequestParam("storageId") int storageId,
                                              @RequestParam("volumeType") String volumeType) {
        return this.volumeService.cloneVolume(description, sourceVolumeId, storageId, volumeType);
    }

    @PutMapping("/api/volume/migrate")
    public ResultUtil<MigrateModel> migrateVolume(
            @RequestParam("sourceVolumeId") int sourceVolumeId,
            @RequestParam("storageId") int storageId,
            @RequestParam("volumeType") String volumeType) {
        return this.volumeService.migrateVolume(sourceVolumeId, storageId, volumeType);
    }

    @PostMapping("/api/volume/resize")
    public ResultUtil<VolumeModel> resizeVolume(
            @RequestParam("volumeId") int volumeId,
            @RequestParam("size") long size) {
        return this.volumeService.resizeVolume(volumeId, size * 1024 * 1024 * 1024);
    }

    @DeleteMapping("/api/volume/destroy")
    public ResultUtil<VolumeModel> destroyVolume(@RequestParam("volumeId") int volumeId) {
        return this.volumeService.destroyVolume(volumeId);
    }

    @DeleteMapping("/api/volume/destroy/batch")
    public ResultUtil<List<VolumeModel>> batchDestroyVolume(@RequestParam("volumeIds") String volumeIdsStr) {
        List<Integer> volumeIds = Arrays.asList(volumeIdsStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
        return this.volumeService.batchDestroyVolume(volumeIds);
    }

    @GetMapping("/api/snapshot/all")
    public ResultUtil<List<SnapshotModel>> listSnapshot() {
        return this.volumeService.listSnapshot();
    }

    @GetMapping("/api/snapshot/info")
    public ResultUtil<SnapshotModel> getSnapshotInfo(@RequestParam("snapshotVolumeId") int snapshotVolumeId) {
        return this.volumeService.getSnapshotInfo(snapshotVolumeId);
    }

    @PutMapping("/api/snapshot/create")
    public ResultUtil<SnapshotModel> createVolumeSnapshot(@RequestParam("volumeId") int volumeId,
                                                          @RequestParam("snapshotName") String snapshotName,
                                                          @RequestParam("snapshotVolumeType") String snapshotVolumeType) {
        return this.volumeService.createVolumeSnapshot(volumeId, snapshotName, snapshotVolumeType);
    }

    @DeleteMapping("/api/snapshot/destroy")
    public ResultUtil<SnapshotModel> destroySnapshot(@RequestParam("snapshotVolumeId") int snapshotVolumeId) {
        return this.volumeService.destroySnapshot(snapshotVolumeId);
    }
}

