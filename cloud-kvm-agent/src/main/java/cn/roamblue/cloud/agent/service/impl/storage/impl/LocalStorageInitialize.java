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
public class LocalStorageInitialize implements StorageInitialize {
    @Override
    public void initialize(Connect connect, String name, String uri, String path, String target) throws LibvirtException {
        StringBuilder sb = new StringBuilder();
        sb.append("<pool type='dir'>")
                .append("<name>").append(name).append("</name>")
                .append("<target>")
                .append("<path>").append(target).append("</path>")
                .append("<permissions>")
                .append("<mode>0711</mode>")
                .append("<owner>0</owner>")
                .append("<group>0</group>")
                .append("</permissions>")
                .append("</target>")
                .append("</pool>");
        String xml= sb.toString();
        connect.storagePoolCreateXML(xml, 0);
    }

    @Override
    public String getType() {
        return StorageType.LOCAL;
    }
}
