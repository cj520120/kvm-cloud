package cn.roamblue.cloud.agent.service.impl.storage.impl;

import cn.roamblue.cloud.agent.service.impl.storage.StorageInitialize;
import cn.roamblue.cloud.common.util.StorageType;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.springframework.stereotype.Component;

/**
 * @ClassName: NfsStorageBuilder
 * @Description: TODO
 * @Create by: chenjun
 * @Date: 2021/8/5 上午11:20
 */
@Component
public class NfsStorageInitialize implements StorageInitialize {
    @Override
    public void initialize(Connect connect, String name, String uri, String path, String target) throws LibvirtException {
        StringBuilder sb = new StringBuilder();
        sb.append("<pool type='netfs' xmlns:fs='http://libvirt.org/schemas/storagepool/fs/1.0'>")
                .append("<name>").append(name).append("</name>")
                .append("<source>")
                .append("<host name='").append(uri).append("'/>")
                .append("<dir path='").append(path).append("'/>")
                .append("</source>")
                .append("<target>")
                .append("<path>").append(target).append("</path>")
                .append("</target>")
                .append("</pool>");
        String xml= sb.toString();
        connect.storagePoolCreateXML(xml, 0);
    }

    @Override
    public String getType() {
        return StorageType.NFS;
    }
}
