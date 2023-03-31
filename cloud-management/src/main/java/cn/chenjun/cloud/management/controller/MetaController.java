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
    @GetMapping(value = "/{version}/meta-data/")
    public void getGuestMetaData(HttpServletRequest request, @RequestHeader("x-real-ip") String ip, HttpServletResponse response) {
        String meta=metaService.getGuestMetaData(ip);
        if(StringUtils.isEmpty(meta)){
            response.setStatus(HttpStatus.OK.value());
        }else {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
        response.getWriter().write(meta);
    }

    @SneakyThrows
    @GetMapping(value = "/{version}/meta-data/{name}")
    public void getGuestMetaValue(@PathVariable("name") String name, @RequestHeader("x-real-ip") String ip, HttpServletResponse response) {
       String meta=metaService.getGuestMetaValue(ip, name);
        if(StringUtils.isEmpty(meta)){
            response.setStatus(HttpStatus.OK.value());
        }else {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
        response.getWriter().write(meta);
    }

    @SneakyThrows
    @GetMapping(value = "/{version}/user-data")
    public void getGuestUserData(HttpServletResponse response) {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("text/cloud-config");
        response.getWriter().write("");
    }
}
