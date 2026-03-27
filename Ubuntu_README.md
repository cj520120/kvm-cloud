### 适用版本
Ubuntu 22.04.3 LTS,其他Ubuntu版本请自行查阅相关配置
### 检查KVM支持
```shell
egrep -c '(vmx|svm)' /proc/cpuinfo

#如果你得到的输出大于0，那么意味着KVM与系统兼容，可以安装
```
### 防火墙配置
```shell
# 放行相关端口
sudo ufw allow 8081/tcp
sudo ufw allow 16509/tcp
```
### 系统升级
```shell
sudo apt update -y
sudo apt upgrade -y
```
### 启用IP转发
```shell
echo "net.ipv4.ip_forward=1" | sudo tee -a /etc/sysctl.conf 
sysctl -p
```
### 编辑网桥文件 
```shell
# 编辑/etc/netplan/00-installer-config.yaml文件(具体文件名请自行查看系统)，调整如下配置: 
cat >/etc/netplan/00-installer-config.yaml<<EOF
network:
  version: 2
  renderer: networkd
  ethernets:
    ens10: #ens10 为实际网卡名称，根据实际情况填写
      dhcp4: false 
  bridges:
    ovs-bridge:
      interfaces: [ ens10 ] #ens10 为实际网卡名称，根据实际情况填写
      addresses: [192.168.6.7/24]  #真实IP，根据实际情况填写
      routes: 
        - to: default
          via: 192.168.6.1 #真实网关，根据实际情况填写
      openvswitch: {}
      nameservers: 
        addresses: [8.8.8.8]  #dns，根据实际情况填写
      parameters: 
        forward-delay: 4
      dhcp4: no
EOF
```
### 安装网络组件
```shell
sudo apt install openvswitch-switch -y
sudo systemctl enable openvswitch-switch
sudo systemctl start openvswitch-switch
```
### 重启网络
```shell
sudo netplan apply 
```
### 查看网桥状态
```shell
sudo ovs-vsctl show
sudo ip a | grep br0
```
### 安装kvm相关组件
```shell
sudo apt install qemu-kvm libvirt-daemon-system ovmf -y
```
### 安装存储池相关，根据实际情况选择安装
```shell
sudo apt install ceph-common libvirt-daemon-driver-storage-rbd -y 
sudo apt install nfs-common -y
sudo apt install glusterfs-client -y
```
### Libvirtd配置
```shell
#编辑/etc/libvirt/qemu.conf文件,调整如下配置:
vnc_listen = "0.0.0.0"
user = "root"
group = "root"
```
### Libvirtd重启
```shell 
sudo systemctl enable --now libvirtd-tcp.socket
```
### JDK安装
```shell
sudo apt install openjdk-8-jdk -y
```
### 安装 supervisor 管理
```shell
sudo apt install supervisor
sudo systemctl start supervisor
sudo systemctl enable supervisor
```
### 安装agent
```shell
sudo mkdir -p /usr/local/cloud
#上传 cloud-agent-1.0-SNAPSHOT.jar到/usr/local/cloud
```
### 创建agent配置文件
```shell
cat <<EOF >> /usr/local/cloud/client.properties
server.port=8081
app.task-thread-size=8
EOF
```

### 创建supervisor管理文件
```shell
cat <<EOF >> /etc/supervisor/conf.d/cloud-agent.conf
[program:Kvm-Cloud-Agent]
command = java -jar cloud-agent-1.0-SNAPSHOT.jar --spring.config.location=client.properties
directory = /usr/local/cloud
autostart = true
startsecs = 5
autorestart = true
startretries = 3
user = root
redirect_stderr = true
stdout_logfile_maxbytes = 20MB
stdout_logfile_backups = 10
stdout_logfile = /var/log/cloud-agent.log
EOF
```
### supervisor加载服务
```shell
sudo supervisorctl update
```
### 查看运行日志
```shell
sudo supervisorctl
tail -f Kvm-Cloud-Agent
```
