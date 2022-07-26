package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.LoginUserTokenInfo;
import cn.roamblue.cloud.management.config.Oauth2Config;
import cn.roamblue.cloud.management.service.UserService;
import cn.roamblue.cloud.management.util.GsonBuilderUtil;
import cn.roamblue.cloud.management.util.Oauth2;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * @author chenjun
 */
@Controller
public class Oauth2LoginController {

    @Autowired
    private Oauth2Config config;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public RedirectView goLoginPage() {
        String redirectUri = URLEncoder.encode(this.config.getRedirectUri(), StandardCharsets.UTF_8);
        return new RedirectView(String.format("%s?response_type=code&client_id=%s&redirect_uri=%s", this.config.getAuthUri(), this.config.getClientId(), redirectUri));
    }

    @PostMapping("/login")
    @ResponseBody
    public ResultUtil<LoginUserTokenInfo> login(@RequestParam("code") String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add(Oauth2.Param.CLIENT_ID, this.config.getClientId());
        params.add(Oauth2.Param.CLIENT_SECRET, this.config.getClientSecret());
        params.add(Oauth2.Param.GRANT_TYPE, Oauth2.GrantType.AUTHORIZATION_CODE);
        params.add(Oauth2.Param.CODE, code);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(this.config.getTokenUri(), HttpMethod.POST, request, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            return ResultUtil.<LoginUserTokenInfo>builder().code(ErrorCode.PERMISSION_ERROR).message(response.getBody()).build();
        }
        Map<String,Object> token = GsonBuilderUtil.create().fromJson(response.getBody(), new TypeToken<Map<String,Object>>(){}.getType());
        headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, String.format("%s %s", token.get("token_type"), token.get("access_token")));
        response = restTemplate.exchange(this.config.getUserUri(), HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            return ResultUtil.<LoginUserTokenInfo>builder().code(ErrorCode.PERMISSION_ERROR).message(response.getBody()).build();
        }
        Map<String,Object> userInfo = GsonBuilderUtil.create().fromJson(response.getBody(), new TypeToken<Map<String,Object>>(){}.getType());
        Object id=userInfo;
        for (String name : this.config.getIdPath()) {
            id=((Map<String,Object>)id).get(name);
        }
        Object authorities=userInfo;
        for (String name : this.config.getAuthoritiesPath()) {
            authorities=((Map<String,Object>)authorities).get(name);
        }
        if(!(authorities instanceof Collection)){
            authorities=null;
        }

        if(id==null){
            return ResultUtil.<LoginUserTokenInfo>builder().code(ErrorCode.PERMISSION_ERROR).message("未找到用户唯一标识，请检查系统配置").build();
        }

        LoginUserTokenInfo tokenInfo= this.userService.loginOauth2(String.valueOf(id),(Collection<String>)authorities);
        return ResultUtil.<LoginUserTokenInfo>builder().data(tokenInfo).build();
    }
}
