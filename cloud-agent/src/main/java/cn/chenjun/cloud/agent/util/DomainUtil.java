package cn.chenjun.cloud.agent.util;

import org.libvirt.Connect;
import org.libvirt.Domain;

import java.util.Objects;

public class DomainUtil {
    public static Domain findDomainByName(Connect connect, String name) throws Exception {
        int[] ids = connect.listDomains();
        for (int id : ids) {
            Domain domain = connect.domainLookupByID(id);
            if (Objects.equals(domain.getName(), name)) {
                return domain;
            } else {
                domain.free();
            }
        }
        String[] namesOfDefinedDomain = connect.listDefinedDomains();
        for (String stopDomain : namesOfDefinedDomain) {
            Domain domain = connect.domainLookupByName(stopDomain);
            if (Objects.equals(domain.getName(), name)) {
                return domain;
            } else {
                domain.free();
            }
        }
        return null;
    }
}
