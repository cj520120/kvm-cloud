package cn.roamblue.cloud.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
@Slf4j
public class ConnectFactory extends BasePooledObjectFactory<Connect> {


    @Override
    public Connect create() throws Exception {
        return new Connect("qemu:///system");
    }

    @Override
    public PooledObject<Connect> wrap(Connect connect) {
        return new DefaultPooledObject<>(connect);
    }

    @Override
    public boolean validateObject(PooledObject<Connect> p) {
        try {
            return p.getObject().isAlive();
        } catch (Exception err) {
            log.error("There was a problem detecting the connection status.", err);
            return false;
        }
    }

    @Override
    public void destroyObject(PooledObject<Connect> p) throws Exception {
        p.getObject().close();
    }

    @Override
    public void activateObject(PooledObject<Connect> p) throws Exception {
        //do nothing
    }

    @Override
    public void passivateObject(PooledObject<Connect> p) throws Exception {
        //do nothing
    }
}
