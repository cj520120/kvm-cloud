package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.management.servcie.MetaService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class MetaController {

    @Autowired
    private MetaService metaService;

    @SneakyThrows
    @GetMapping(value = "/meta-data")
    public void listGuestMetaData(HttpServletRequest request, @RequestHeader(value = "x-real-ip",defaultValue = "127.0.0.1") String ip,  @RequestHeader("x-sign") String sign, HttpServletResponse response) {
        String allMetaData=metaService.loadAllGuestMetaData(ip,sign);
        if(StringUtils.isEmpty(allMetaData)){
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.getWriter().write("not found");
        }else {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("text/cloud-config;charset=utf-8");
            response.getWriter().write(allMetaData);
        }
    }

    @SneakyThrows
    @GetMapping(value = "/user-data")
    public void listGuestUserData(HttpServletResponse response,  @RequestHeader("x-sign") String sign) {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("text/cloud-config;charset=utf-8");
        response.getWriter().write("#cloud-config\r\n");
    }
}
