package cn.roamblue.cloud.agent.service.impl.storage;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

import java.util.Map;

/**
 * @ClassName: StorageService
 * @Description: TODO
 * @Create by: chenjun
 * @Date: 2021/8/5 上午11:19
 */
public interface StorageInitialize  {
    /**
     * 创建存储池xml
     * @param connect
     * @param name
     * @param uri
     * @param path
     * @param target
     * @return
     * @throws LibvirtException
     */
    void initialize(Connect connect, String name, String uri, String path, String target) throws LibvirtException;


    /**
     * 存储池类型
     * @return
     */
    String getType();
}
