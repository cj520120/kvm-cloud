English | [中文](README.md)
### Project Introduction
KVM Cloud is a lightweight private cloud platform implemented in Java. It is designed to help small and medium-sized enterprises quickly manage computing, storage, network, and other resources, enabling them to build their own cloud platforms. Its features include, but are not limited to:
- Basic KVM-based VM functions (create, start, stop, reinstall, webVNC, etc.)
- Support for NFS, GlusterFS, and Ceph RBD disk storage pools
- Support for dynamic disk attachment and detachment
- Multi-host management
- Template management for rapid VM creation
- Automatic VM IP address management
- Multiple NIC support
- Support for cloud-init initialization
- Support for internal DNS resolution
- Support for NAT forwarding
- Support for key management and login
- Support for hostname customization

## 📌 Commercially Licensed Edition (Ruiyun kvm-cloud)
> This repository serves as the open-source baseline. A separate commercially licensed enhanced edition is also available:
> - Open-source edition: Licensed under the repository's original open-source license and intended for learning and evaluation only, with no commercial SLA or value-added components.
> - Commercially licensed edition: Ruiyun kvm-cloud provides enterprise enhancements such as multi-cluster management, PCI passthrough, SDN networking, offline licensing, overlay images, monitoring, and disk snapshots. **The commercial edition's source code is not included in this repository and is not covered by this repository's open-source license. A separate commercial license is required for use.**
>
> For the Ruiyun commercially licensed edition, please contact official customer service through [Contact Us](https://www.ruiyuncloud.cn/#contact) or visit the [official website](https://www.ruiyuncloud.cn/) for details.

## Version Comparison
| Feature | Open-source Edition | Subscription Edition | Description |
| --- | --- | --- | --- |
| Multi-cluster management | ❌ | ✅ |  |
| PCI passthrough | ❌ | ✅ | The subscription edition supports PCI passthrough for VMs bound to a host. |
| Networking | FLAT, VLAN | FLAT, VLAN, VXLAN | The subscription edition provides SDN management services and supports VXLAN networks. |
| Security groups | ❌ | ✅ | The subscription edition generates dynamic security group flows based on the OpenFlow 1.3 protocol. |
| Cluster features | ❌ | ✅ |  |
| Storage pools | Ceph RBD/NFS/GlusterFS/Local | Ceph RBD/CephFS/NFS/GlusterFS/Local |  |
| Overlay disks | ❌ | ✅ | The subscription edition supports overlay disks to save disk space. |
| Monitoring | ❌ | ✅ | The subscription edition supports VMs, hosts, and other resources. It requires Prometheus and provides visualization pages for basic memory, disk, and other metrics. |
| Disk snapshots | ❌ | ✅ | The subscription edition supports VM disk snapshots, automatic on-demand template downloads, automatic storage pool synchronization, and other features. |
| API documentation | ❌ | ✅ | The subscription edition integrates API documentation. |
| AppKey access | ❌ | ✅ | The subscription edition provides API key access for secondary integration. |
| Bridge type | Linux Bridge, OVS Bridge | OVS Bridge |  |
| Fault domains | ❌ | ✅ | The subscription edition provides host isolation configuration, with VMs belonging to their respective fault domains. |
| Template and disk upload | ❌ | ✅ |  |
| Batch operations | Partially supported | ✅ | Supports batch creation, startup, shutdown, and other operations. |
| Audit logs | ❌ | ✅ |  |

### Operating System
Linux (currently only the x86_64 architecture is supported)

### Repository Hosting
[AtomGit](https://gitcode.com/cj520120/kvm-cloud) <br/>
[Gitee](https://gitee.com/cj520120/kvm-cloud) <br/>
[GitHub](https://github.com/cj520120/kvm-cloud) <br/>
If this project is helpful to you, please star and fork it. Your star is the greatest support for us.

### Configuration Notes
This document provides configuration instructions for both CentOS 7 and Ubuntu 22.04. Under each configuration item, the steps for both systems are listed side by side; choose the one that matches your system. For other operating systems, please consult the relevant documentation. The supported Ubuntu version is 22.04.3 LTS; for other Ubuntu versions, please consult the relevant documentation (the original standalone document is [Ubuntu_README.md](Ubuntu_README.md)).

### System Upgrade (Ubuntu)
```shell
sudo apt update -y
sudo apt upgrade -y
```
### SELinux Configuration
> This section applies to CentOS 7 only. Ubuntu uses AppArmor by default and does not enable SELinux, so you can skip this section.
```sh
setenforce 0
vi /etc/selinux/config
SELINUX=permissive
```

### Sysctl Configuration (Enable IP Forwarding)
**CentOS 7**
```sh
vim /etc/sysctl.conf
net.ipv4.ip_forward=1               # Enable forwarding and save
sysctl -p
```
**Ubuntu**
```shell
echo "net.ipv4.ip_forward=1" | sudo tee -a /etc/sysctl.conf
sysctl -p
```

### Firewall Configuration
**CentOS 7** (stop the firewall and related services)
```sh
systemctl stop firewalld
systemctl disable firewalld
systemctl stop iptables
systemctl disable iptables
systemctl stop NetworkManager
systemctl disable NetworkManager
```
**Ubuntu** (allow the required ports)
```shell
sudo ufw allow 8081/tcp
sudo ufw allow 16509/tcp
```

### NFS Configuration
#### 1. Install NFS Packages
**CentOS 7** (server)
```sh
yum -y install nfs-utils rpcbind
```
**Ubuntu** (client; install as needed when used as a storage pool)
```shell
sudo apt install nfs-common -y
```

#### 2. Enable Services at Boot and Start Them (CentOS 7 server)
```sh
systemctl enable rpcbind
systemctl enable nfs-server
systemctl start rpcbind
systemctl start nfs-server
```

#### 3. Create a Storage Directory
```sh
mkdir -p /data/nfs
```

#### 4. Edit the Configuration
```sh
vi /etc/exports
/data/nfs *(rw,sync,no_root_squash)
```

#### 5. Reload the exportfs File
```sh
exportfs -a
```

#### 6. Restart the Services
```sh
systemctl restart rpcbind
systemctl restart nfs-server
```

#### 7. Test Whether Mounting Works
```sh
mount -t nfs 127.0.0.1:/data/nfs /mnt
df -h        ### The mount is successful if it appears in the output
umount /mnt
```

### KVM
#### 1. Verify That the Host Supports Virtualization
**CentOS 7**
```sh
lsmod | grep kvm # Check the output to confirm whether virtualization is supported
If the host is a VMware virtual machine, enable virtualization in the VM settings.
```
**Ubuntu**
```shell
egrep -c '(vmx|svm)' /proc/cpuinfo
# An output greater than 0 means virtualization is supported and KVM can be installed
```

#### 2. Install KVM on the Host
**CentOS 7**
```sh
yum install qemu-kvm libvirt bridge-utils
yum install java-1.8.0-openjdk* -y
```
**Ubuntu**
```shell
sudo apt install qemu-kvm libvirt-daemon-system ovmf -y
```

#### 2.1 Install Storage Pool Components (Ubuntu; install as needed)
```shell
sudo apt install ceph-common libvirt-daemon-driver-storage-rbd -y
sudo apt install nfs-common -y
sudo apt install glusterfs-client -y
```

#### 2.2 Install JDK (Ubuntu)
```shell
sudo apt install openjdk-8-jdk -y
```

#### 3. Configure a Network Bridge on the KVM Host
**Important:** Use `ip addr` to check your `network interface name`. In CentOS 7, the interface name may not be `eth0`. An incorrect interface name will prevent the VMs configured later from being accessed properly.

After confirming the correct interface name, configure the bridge:

1) Create the bridge interface file `/etc/sysconfig/network-scripts/ifcfg-br0`.
```sh
DEVICE=br0
TYPE=Bridge
ONBOOT=yes
BOOTPROTO=static
IPADDR=192.168.2.130
NATMASK=255.255.255.0
PREFIX=24
GATEWAY=192.168.2.1
DNS1=8.8.4.4
DNS2=8.8.8.8
```

2) Modify the default network interface configuration.

> **Change the filename to match the actual interface name. For example, if the interface is eth0, the corresponding file is `/etc/sysconfig/network-scripts/ifcfg-eth0`.**
```sh
BOOTPROTO=none
NAME=eth0
DEVICE=eth0
ONBOOT=yes
NM_CONTRLLED=no
BRIDGE=br0
```

3) For OVS configuration, refer to [ovs-bridge-sample.md](scripts/ovs-bridge-sample.md).

**Ubuntu** (configure an OVS bridge via netplan)
1) Install the OVS network components
```shell
sudo apt install openvswitch-switch -y
sudo systemctl enable openvswitch-switch
sudo systemctl start openvswitch-switch
```
2) Edit `/etc/netplan/00-installer-config.yaml` (check the actual filename on your system) and adjust it as follows:
```yaml
network:
  version: 2
  renderer: networkd # Set as appropriate, e.g. networkd, NetworkManager
  ethernets:
    ens10: # ens10 is the actual interface name; set as appropriate
      dhcp4: false
  bridges:
    ovs-bridge:
      interfaces: [ ens10 ] # ens10 is the actual interface name; set as appropriate
      addresses: [192.168.6.7/24]  # Real IP address; set as appropriate
      routes:
        - to: default
          via: 192.168.6.1 # Real gateway; set as appropriate
      openvswitch: {}
      nameservers:
        addresses: [8.8.8.8]  # DNS; set as appropriate
      parameters:
        forward-delay: 4
      dhcp4: no
```
3) Restart the network and check the bridge status
```shell
sudo netplan apply
sudo ovs-vsctl show
sudo ip a | grep ovs-bridge
```

#### 4. VNC Configuration
**Common to CentOS 7 / Ubuntu**
```sh
vi /etc/libvirt/qemu.conf
vnc_listen="0.0.0.0"
user = "root"
group = "root"
```

#### 5. Libvirtd Configuration
**CentOS 7**
1) Modify the libvirtd configuration.
```sh
vi /etc/libvirt/libvirtd.conf
listen_tls = 0
listen_tcp = 1
unix_sock_group = "root"
unix_sock_rw_perms = "0777"
auth_unix_ro = "none"
auth_unix_rw = "none"
tcp_port = "16509"
listen_addr = "0.0.0.0"
auth_tcp = "none"

vi /etc/sysconfig/libvirtd
LIBVIRTD_ARGS="--listen"
systemctl restart libvirtd
```
**Ubuntu**
```shell
sudo systemctl enable --now libvirtd-tcp.socket
```

#### Project Compilation
```sh
mvn clean package -Dfile.encoding=UTF-8 -DskipTests=true
```

### Completing the Configuration
1. Import the MySQL tables and related data.
> **The scripts are located in the `scripts` directory.**

2. Modify the configuration files.

3. Start the management and agent services separately, then open http://localhost:8080/ in a browser.

**CentOS 7** (start directly from the command line)
```sh
Management: java -jar cloud-management-1.0-SNAPSHOT.jar --spring.config.location=server.yaml
Agent: java -jar cloud-agent-1.0-SNAPSHOT.jar --spring.config.location=client.properties
--spring.config.location is optional and specifies the configuration file. It can be omitted if no changes are needed. The configuration files are application.properties in the src/main/resources directory of each module.
```

**Ubuntu** (manage the Agent with supervisor)
1) Install supervisor
```shell
sudo apt install supervisor
sudo systemctl start supervisor
sudo systemctl enable supervisor
```
2) Install the agent, upload `cloud-agent-1.0-SNAPSHOT.jar` to `/usr/local/cloud`, and create the configuration file
```shell
sudo mkdir -p /usr/local/cloud
cat <<EOF >> /usr/local/cloud/client.properties
server.port=8081
app.task-thread-size=8
EOF
```
3) Create the supervisor configuration file
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
4) Reload the service and view the runtime log
```shell
sudo supervisorctl update
sudo supervisorctl
tail -f Kvm-Cloud-Agent
```

4. The default platform login username/password is admin/123456.

5. Create a basic network.

> **Use bridged networking. The IP address range must be in the same subnet as the host. You can isolate the range between the start and end IP addresses from the host network to prevent IP conflicts. VLAN is only supported with OVS.**

![](images/en/network.png)

6. Create a host.

![](images/en/host.png)

7. Create a storage pool.
> Templates can only use shared storage pools.

![](images/en/storage.png)

8. Download a base template.
```sh
Use an official Red Hat-based image with iptables support. For example:
> https://yum.oracle.com/templates/OracleLinux/OL10/u1/x86_64/OL10U1_x86_64-kvm-b270.qcow2

Do not use Chinese Linux distribution images. Chinese distributions vary in their support for cloud-init, which may cause system initialization to fail. If you need to use one, test its compatibility yourself.

Alternative download (if the official link is unavailable)
Template path: cloud/v5/OL10U1_x86_64-kvm-b270.qcow2
Baidu Netdisk: https://pan.baidu.com/s/1bOAeuvFj8hG4skDaoZnYtQ?pwd=1bpn Access code: 1bpn

Notes
1. The system template downloads and installs packages over the network during initialization, so ensure that the network is properly configured.
2. Initialization time depends on network speed. You can use the virsh console command to log in to the system and check the initialization status.

Default template credentials
Default username: kvm-user
Default password: Kvm@123456
```

9. Configure the system template.

![](images/en/template.png)

10. Wait for the system template to finish downloading and the system VM to initialize successfully.

11. When attaching additional disks to Windows, install the virtio-win.iso driver.

12. Create a VM.

![](images/en/create-vm.png)

![](images/en/vm.png)

![](images/en/vm-info.png)

### Related Issues

1. Configuration file not found, causing database connection problems
```sh
server.yaml and client.properties correspond to application.yaml and application.properties in the management and agent projects, respectively. Rename the files and modify the relevant configuration as needed before running.
```

2. Backup and recovery
```sh
Perform full backups of the database and storage pools.
Data is invaluable. It is recommended that you back up the data in your virtual machines.
```

3. Network isolation
```sh
Currently, VLAN mode is only supported with OVS bridges. If needed, install OVS yourself.
```

4. Some Windows systems cannot find a boot device
```sh
1. First, confirm that the ISO system type selected during creation is correct.
2. If the system type is correct, use a tool such as LaoMaoTao to create a PE ISO image and use it when creating the system. After entering the PE system, unmount the disc on the page and mount the ISO of the operating system you want to install. You can then install the operating system normally through PE.
```

5. Windows systems do not recognize disks
```sh
1. Windows does not include virtio drivers. Install the virtio-win.iso driver.
2. If the affected disk is the system disk, temporarily mount the virtio-win.iso driver on the disk selection page during installation, or select the IDE bus.
```

6. Recovery after a server power failure and restart
```sh
A power failure may corrupt virtual disks. If a VM cannot start, use qemu-img check to inspect the disk and perform the appropriate repairs.
```

7. Nested virtualization for virtual machines

- Verify whether nested virtualization is enabled on the KVM host:
```sh
For Intel processors, run: cat /sys/module/kvm_intel/parameters/nested
For AMD processors, run: cat /sys/module/kvm_amd/parameters/nested
An output of N or 0 means nested virtualization is disabled. An output of Y or 1 means nested virtualization is enabled on the host.
```

- To enable nested virtualization, create a file named `/etc/modprobe.d/kvm-nested.conf` with the following contents:
```sh
options kvm-intel nested=1
options kvm-intel enable_shadow_vmcs=1
options kvm-intel enable_apicv=1
options kvm-intel ept=1
```

- Reboot the host.
- Verify that nested virtualization is now enabled.
```sh
cat /sys/module/kvm_intel/parameters/nested
```

8. cloud-init configuration
- cloud-init uses the NoCloud data source. Modify the cloud configuration as follows:
```yaml
datasource:
  NoCloud:
    seedfrom: http://169.254.169.254/
datasource_list: [  NoCloud ]
```
- The user template must have qemu-guest-agent installed and guest-exec enabled; otherwise, startup initialization cannot be configured.

9. After deleting a host from the page, if you need to add it again, delete `config.json` from the Agent directory on that host and restart the Agent.

10. For signature errors, ensure that the system time is synchronized between the management and agent services.

11. Modify the default machine settings
```sh
Adjust the system configuration properties vm.machine.name and vm.machine.arch.
The supported values depend on the operating system. Use the following commands to query them:
CentOS: /usr/libexec/qemu-kvm -machine help
Ubuntu: qemu-system-i386 -machine help
```

12. Network connectivity problems
```sh
Use a clean operating system installation whenever possible. If the network is unavailable, check the firewall, iptables, and other related settings.
```

13. VMs fail to start on Ubuntu
```sh
Ubuntu reports: qemu-system-x86_64: unable to map backing store for guest RAM: Cannot allocate memory
Disable huge page caching by modifying vm.memory.huge.pages.enable and vm.memory.huge.pages.size, or append vm.nr_hugepages=10240 to /etc/sysctl.conf. Adjust the value according to your actual requirements.
```
