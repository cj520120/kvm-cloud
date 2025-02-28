package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.management.servcie.MetaService;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Controller
public class MetaController {

    @Autowired
    private MetaService metaService;

    @SneakyThrows
    @GetMapping(value = "/meta-data")
    public void listGuestMetaData(HttpServletRequest request, @RequestHeader(value = "X-Network-ID", defaultValue = "0") int networkId,@RequestHeader(value = "X-Real-IP", defaultValue = "127.0.0.1") String ip, @RequestHeader("X-Nonce") String nonce, @RequestHeader("X-Sign") String sign, HttpServletResponse response) {
        MetaData metaData = metaService.loadAllGuestMetaData(networkId, ip, nonce, sign);
        response.setStatus(HttpStatus.OK.value());
        response.getWriter().write(buildMetaResponse(Collections.singletonList(metaData)));
    }

    @SneakyThrows
    @GetMapping(value = "/{meta_path}/meta-data/{name}")
    public void findMetaDataByKey(HttpServletRequest request, @PathVariable("key") String key, @RequestHeader(value = "X-Network-ID", defaultValue = "0") int networkId, @RequestHeader(value = "X-Real-IP", defaultValue = "127.0.0.1") String ip, @RequestHeader("X-Nonce") String nonce, @RequestHeader("X-Sign") String sign, HttpServletResponse response) {
        String allMetaData = metaService.findMetaDataByKey(key, networkId, ip, nonce, sign);
        response.setStatus(HttpStatus.OK.value());
        response.getWriter().write(allMetaData);
    }
    @SneakyThrows
    @GetMapping(value = "/vendor-data")
    public void findGuestVendorData(HttpServletRequest request, @RequestHeader(value = "X-Network-ID", defaultValue = "0") int networkId,@RequestHeader(value = "X-Real-IP", defaultValue = "127.0.0.1") String ip, @RequestHeader("X-Nonce") String nonce, @RequestHeader("X-Sign") String sign, HttpServletResponse response) {
        List<MetaData> partList = this.metaService.findGuestVendorData(networkId, ip, nonce, sign);
        response.setStatus(HttpStatus.OK.value());
        response.getWriter().write(buildMetaResponse(partList));
    }
    @SneakyThrows
    @GetMapping(value = "/user-data")
    public void listGuestUserData(HttpServletResponse response, @RequestHeader(value = "X-Network-ID", defaultValue = "0") int networkId, @RequestHeader(value = "X-Real-IP", defaultValue = "127.0.0.1") String ip, @RequestHeader("X-Nonce") String nonce, @RequestHeader("X-Sign") String sign) {
        List<MetaData> partList = this.metaService.findGuestInitData(networkId, ip, nonce, sign);
        response.setStatus(HttpStatus.OK.value());
        response.getWriter().write(buildMetaResponse(partList));
    }

    private String buildMetaResponse(List<MetaData> partList) {
        partList = partList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        if (partList != null && !partList.isEmpty()) {
            if (partList.size() == 1) {
                MetaData metaData = partList.get(0);
                sb.append(metaData.getType().getFirstLine());
                sb.append(metaData.getBody());
            } else {
                String boundary = UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT);
                sb.append("Content-Type: multipart/mixed; boundary=\"").append(boundary).append("\"\n");
                sb.append("MIME-Version: 1.0\n");
                sb.append("Number-Attachments: ").append(partList.size()).append("\n\n");
                for (int i = 0; i < partList.size(); i++) {
                    MetaData metaData = partList.get(i);
                    sb.append(buildConfig(metaData, boundary, i));
                }
                sb.append("--").append(boundary).append("--");
            }
        }
        return sb.toString();
    }

    private String buildConfig(MetaData metaData, String boundary, int index) {
        String firstLine = metaData.getType().getFirstLine();
        String contextType = metaData.getType().getContextType();
        String config = metaData.getBody().replace(firstLine, "").trim();
        String sb = "--" + boundary + "\n" +
                "Content-Type: " + contextType + "; charset=\"utf-8\"\n" +
                "MIME-Version: 1.0\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Disposition: attachment; filename=\"part-00" + index + "\"\n\n" +
                config +
                "\n";
        return sb;
    }
}
