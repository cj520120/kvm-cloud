package cn.chenjun.cloud.management.data.dao;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.management.data.entity.UserEntity;
import cn.chenjun.cloud.management.data.mapper.UserInfoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Repository
public class UserInfoDao {
    @Autowired
    private UserInfoMapper mapper;

    public UserEntity findById(int id) {
        return mapper.selectById(id);
    }

    public List<UserEntity> listAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    public Page<UserEntity> search(String keyword, int no, int size) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        if (!ObjectUtils.isEmpty(keyword)) {
            String condition = "%" + keyword + "%";
            queryWrapper.like(UserEntity.LOGIN_NAME, condition);

        }
        int nCount = Math.toIntExact(this.mapper.selectCount(queryWrapper));
        int nOffset = (no - 1) * size;
        queryWrapper.last("limit " + nOffset + ", " + size);
        List<UserEntity> list = this.mapper.selectList(queryWrapper);
        Page<UserEntity> page = Page.create(nCount, nOffset, size);
        page.setList(list);
        return page;
    }

    public void deleteById(int id) {
        mapper.deleteById(id);
    }

    public void update(UserEntity entity) {
        mapper.updateById(entity);
    }

    public UserEntity insert(UserEntity entity) {
        mapper.insert(entity);
        return entity;
    }

    public UserEntity findByLoginNameAndLoginType(short loginType, String loginName) {
        return mapper.selectOne(new QueryWrapper<UserEntity>().eq(UserEntity.LOGIN_NAME, loginName).eq(UserEntity.LOGIN_TYPE, loginType));
    }
}
