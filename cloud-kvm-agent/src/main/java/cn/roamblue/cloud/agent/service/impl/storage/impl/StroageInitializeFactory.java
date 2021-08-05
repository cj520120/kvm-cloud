package cn.roamblue.cloud.agent.service.impl.storage.impl;

import cn.roamblue.cloud.agent.service.impl.storage.StorageInitialize;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
/**
 * @author chenjun
 * @ClassName: StroageInitializeProxy
 * @Description: TODO
 * @Create by: chenjun
 * @Date: 2021/8/5 上午11:24
 */
@Component
public class StroageInitializeFactory {
    @Autowired
    private List<StorageInitialize> storageList;

    public StorageInitialize find(String type){
        return storageList.stream().filter(t->t.getType().equals(type)).findFirst().orElseThrow(()->new CodeException(ErrorCode.NOT_SUPPORTED,"cannot suport stoage:"+type));
    }
}
