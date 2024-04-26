#!/usr/bin/env bash

# 检查系统是否启用KVM虚拟化
if ! grep -q 'vmx' /proc/cpuinfo; then
    # 检查是否可以启用虚拟化
    if ! grep -q '^flags' /proc/cpuinfo | grep -qw 'vmx'; then
        echo "This system does not support KVM virtualization. Exiting..."
        exit 1
    else
        # 启用虚拟化
        echo "Enabling virtualization support..."
        modprobe kvm
        echo "options kvm-intel nested=1" > /etc/modprobe.d/kvm-intel.conf
        echo "Virtualization support has been enabled. Please reboot the system for the changes to take effect."
        exit 0
    fi
fi

# 检查Java环境
if ! command -v java >/dev/null 2>&1; then
    echo "Java is not installed. Installing OpenJDK 1.8..."
    yum install -y java-1.8.0-openjdk
fi

# 检查libvirtd是否安装
if ! command -v libvirtd >/dev/null 2>&1; then
    echo "libvirtd is not installed. Installing libvirt..."
    yum install -y libvirt libvirt-client libvirt-python qemu-kvm virt-install virt-viewer virt-manager
    systemctl start libvirtd
    systemctl enable libvirtd
fi

# 检查是否启用VNC
sed -i 's/#vnc_listen = "0.0.0.0"/vnc_listen = "0.0.0.0"/g' /etc/libvirt/qemu.conf
sed -i 's/#group = "root"/group = "root"/g' /etc/libvirt/qemu.conf
sed -i 's/#user = "root"/user = "root"/g' /etc/libvirt/qemu.conf
systemctl restart libvirtd
if [ ! -e "/etc/yum.repos.d/firmware.repo" ]; then
    # 下载文件
    curl -sSL "http://www.kraxel.org/repos/firmware.repo" -o "/etc/yum.repos.d/firmware.repo"
fi

# 检查UEFI相关组件是否安装
if ! rpm -q OVMF >/dev/null 2>&1; then
    echo "Installing UEFI components..."
    yum install -y OVMF
fi

# 检查edk2.git-ovmf-x64是否安装
if ! rpm -q edk2.git-ovmf-x64 >/dev/null 2>&1; then
    echo "Installing edk2.git-ovmf-x64..."
    yum install -y edk2.git-ovmf-x64
fi

# 获取可用的网卡列表
available_nics=$(ip link show | awk '/state UP/ {print $2}' | sed 's/://' | tr '\n' ' ')

# 如果没有可用网卡,退出脚本
if [ -z "$available_nics" ]; then
    echo "No available network interfaces found."
    exit 1
fi

# 显示可用网卡列表,让用户选择
echo "Available network interfaces:"
select nic in $available_nics; do
    if [ -n "$nic" ]; then
        break
    else
        echo "Invalid selection. Please try again."
    fi
done

# 让用户输入桥接网卡名称
read -p "Enter the name for the bridge interface: " bridge_name

# 获取静态IP设置
read -p "Enter the IP address: " ip_address
read -p "Enter the netmask: " netmask
read -p "Enter the gateway: " gateway
read -p "Enter the DNS server (separate multiple entries with spaces): " dns_servers



# 创建桥接网卡配置文件
bridge_conf="/etc/sysconfig/network-scripts/ifcfg-$bridge_name"
slave_conf="/etc/sysconfig/network-scripts/ifcfg-$nic"

cat << EOF > $bridge_conf
DEVICE=$bridge_name
TYPE=Bridge
BOOTPROTO=static
ONBOOT=yes
DELAY=0
IPADDR=$ip_address
NETMASK=$netmask
GATEWAY=$gateway
DNS1=$(echo $dns_servers | awk '{print $1}')
DNS2=$(echo $dns_servers | awk '{print $2}')
EOF

cat << EOF > $slave_conf
DEVICE=$nic
TYPE=Ethernet
BOOTPROTO=none
ONBOOT=yes
BRIDGE=$bridge_name
EOF

# 重启网络服务使更改生效
systemctl restart network

echo "Bridge interface $bridge_name has been created and $nic has been added as a slave."

echo "The bridge interface has been configured with the following static IP settings:"
echo "IP Address: $ip_address"
echo "Netmask: $netmask"
echo "Gateway: $gateway"
echo "DNS Servers: $dns_servers"
echo "Configuration success."
