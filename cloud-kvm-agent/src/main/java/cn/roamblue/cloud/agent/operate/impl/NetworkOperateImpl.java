package cn.roamblue.cloud.agent.operate.impl;

import cn.hutool.core.util.RuntimeUtil;
import cn.roamblue.cloud.agent.operate.NetworkOperate;
import cn.roamblue.cloud.common.agent.NetworkRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.libvirt.Connect;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
public class NetworkOperateImpl implements NetworkOperate {

    public static void main(String[] args) throws SocketException {
        System.out.println(NetworkInterface.getByName("en01"));
    }


    @Override
    public void create(Connect connect, NetworkRequest request) throws Exception {
        NetworkRequest.BasicBridge basicBridge = request.getBasicBridge();
        NetworkInterface basicNic=NetworkInterface.getByName(basicBridge.getBridge());
        if(basicNic==null){
            if(null==NetworkInterface.getByName(basicBridge.getNic())){
                throw new CodeException(ErrorCode.SERVER_ERROR,"未找到物理网卡:"+basicBridge.getNic());
            }
            //创建网卡 brctl addbr eth0.100.br100 && brctl stp eth0.100.br100 on
            basicNic=NetworkInterface.getByName(basicBridge.getBridge());
        }
        if(basicNic.getParent()==null){
            //添加关联 brctl addif eth0.100.br100 eth0.100
            basicNic=NetworkInterface.getByName(basicBridge.getBridge());
        }
        if(!basicNic.isUp()){
           //启用网卡 ip link set eth0.100.br100 up
        }


    }

    @Override
    public void destroy(Connect connect, String name) throws Exception {

    }
}
