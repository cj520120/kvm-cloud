package com.roamblue.cloud.management.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roamblue.cloud.management.data.entity.LoginInfoEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjun
 */
@Repository
public interface LoginInfoMapper extends BaseMapper<LoginInfoEntity> {
    /**
     * 获取登陆用户
     *
     * @param loginName
     * @return
     */
    @Select(value = "SELECT * FROM tbl_login_info WHERE login_name=#{name}")
    LoginInfoEntity findByLoginName(@Param("name") String loginName);

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    @Select(value = "SELECT * FROM tbl_login_info WHERE user_id=#{userId}")
    LoginInfoEntity findById(@Param("userId") int userId);

    /**
     * 获取所有用户
     *
     * @return
     */
    @Select(value = "SELECT * FROM tbl_login_info")
    List<LoginInfoEntity> findAll();
}
