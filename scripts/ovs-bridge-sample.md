### 网卡配置
```$xslt
cat /etc/sysconfig/network-scripts/ifcfg-eth0
DEVICE=eth0
TYPE=OVSPort
DEVICETYPE=ovs
NAME=eth0
OVS_BRIDGE=ovs-bridge
ONBOOT=yes
```
### 桥接配置
```$xslt
cat /etc/sysconfig/network-scripts/ifcfg-ovs-bridge
DEVICE=ovs-bridge
DEVICETYPE=ovs
TYPE=OVSBridge
BOOTPROTO=static
IPADDR=192.168.1.89
NETMASK=255.255.255.0
GATEWAY=192.168.1.1
DNS1=8.8.8.8
DNS2=114.114.114.114
ONBOOT=yes
```

