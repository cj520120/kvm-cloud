package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.management.servcie.MetaService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        String allMetaData = metaService.loadAllGuestMetaData(networkId,ip, nonce, sign);
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("text/cloud-config;charset=utf-8");
        response.getWriter().write(allMetaData);
    }

    @SneakyThrows
    @GetMapping(value = "/vendor-data")
    public void findGuestVendorData(HttpServletRequest request, @RequestHeader(value = "X-Network-ID", defaultValue = "0") int networkId,@RequestHeader(value = "X-Real-IP", defaultValue = "127.0.0.1") String ip, @RequestHeader("X-Nonce") String nonce, @RequestHeader("X-Sign") String sign, HttpServletResponse response) {
        String allMetaData = metaService.findGuestVendorData(networkId,ip, nonce, sign);
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("text/cloud-config;charset=utf-8");
        response.getWriter().write(allMetaData);
    }
    @SneakyThrows
    @GetMapping(value = "/user-data")
    public void listGuestUserData(HttpServletResponse response, @RequestHeader(value = "X-Network-ID", defaultValue = "0") int networkId, @RequestHeader(value = "X-Real-IP", defaultValue = "127.0.0.1") String ip, @RequestHeader("X-Nonce") String nonce, @RequestHeader("X-Sign") String sign) {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("text/cloud-config;charset=utf-8");
        response.getWriter().write(this.metaService.loadAllGuestUserData(networkId,ip, nonce, sign));
    }
}
