#ubuntu 安装
sudo apt install vlan bridge-utils  net-tools qemu-kvm libvirt-daemon-system libvirt-clients selinux-utils
systemctl enable libvirtd && systemctl start libvirtd
#安装nfs
sudo apt-get install nfs-kernel-server
sudo apt-get install nfs-common
sudo vim /etc/exports

/data/nfs *(rw,sync,no_root_squash)

systemctl enable nfs-kernel-server && systemctl start nfs-kernel-server

sudo chmod -R 644 /data/nfs

sudo mount -o resvport -o nolock -t nfs 192.168.1.69:/data/nfs ~/Code/nfs
rm -rf  ~/Code/nfs/code/gitee-kvm-cloud/cloud-kvm-agent/
rm -rf  ~/Code/nfs/code/gitee-kvm-cloud/cloud-common/
rm -rf  ~/Code/nfs/code/gitee-kvm-cloud/cloud-management/
cp -r ./gitee-kvm-cloud/cloud-kvm-agent ~/Code/nfs/code/gitee-kvm-cloud/
cp -r ./gitee-kvm-cloud/cloud-common ~/Code/nfs/code/gitee-kvm-cloud/
cp -r ./gitee-kvm-cloud/cloud-management ~/Code/nfs/code/gitee-kvm-cloud/


