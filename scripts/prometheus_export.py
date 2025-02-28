import libvirt
import time
from prometheus_client import start_http_server, Gauge

# 连接到本地 KVM
def connect_to_kvm():
    conn = libvirt.open("qemu:///system")
    if conn is None:
        print("Failed to connect to KVM")
        exit(1)
    return conn

# 定义 Prometheus 指标
memory_usage_gauge = Gauge('kvm_vm_memory_usage_percent', 'Memory usage percentage of the VM', ['vm_name'])
cpu_usage_gauge = Gauge('kvm_vm_cpu_usage_percent', 'CPU usage percentage of the VM', ['vm_name'])
disk_read_rate_gauge = Gauge('kvm_vm_disk_read_rate_bytes', 'Disk read rate in bytes/s', ['vm_name', 'disk_device'])
disk_write_rate_gauge = Gauge('kvm_vm_disk_write_rate_bytes', 'Disk write rate in bytes/s', ['vm_name', 'disk_device'])
network_rx_rate_gauge = Gauge('kvm_vm_network_rx_rate_bytes', 'Network receive rate in bytes/s', ['vm_name', 'iface_name'])
network_tx_rate_gauge = Gauge('kvm_vm_network_tx_rate_bytes', 'Network transmit rate in bytes/s', ['vm_name', 'iface_name'])

# 获取虚拟机的内存使用率
def get_memory_usage(domain):
    mem_stats = domain.memoryStats()
    if "available" in mem_stats and "unused" in mem_stats:
        available_mem = mem_stats["available"]
        unused_mem = mem_stats["unused"]
        used_mem = available_mem - unused_mem
        memory_usage = (used_mem / available_mem) * 100
        return round(memory_usage, 2)
    return None

# 获取虚拟机的 CPU 使用率
def get_cpu_usage(domain):
    cpu_stats = domain.getCPUStats(True)
    if cpu_stats:
        cpu_time = cpu_stats[0]["cpu_time"]
        time.sleep(1)  # 等待 1 秒
        cpu_stats_new = domain.getCPUStats(True)
        cpu_time_new = cpu_stats_new[0]["cpu_time"]
        cpu_usage = (cpu_time_new - cpu_time) / 10000000  # 转换为百分比
        return round(cpu_usage, 2)
    return None

# 获取虚拟机的磁盘 IO 速率（字节/s）
def get_disk_io_rate(domain, disk_device):
    disk_stats = domain.blockStats(disk_device)
    if disk_stats:
        read_bytes_prev, write_bytes_prev = disk_stats[0], disk_stats[1]
        time.sleep(1)  # 等待 1 秒
        disk_stats_new = domain.blockStats(disk_device)
        read_bytes_new, write_bytes_new = disk_stats_new[0], disk_stats_new[1]
        read_rate = read_bytes_new - read_bytes_prev  # 字节/s
        write_rate = write_bytes_new - write_bytes_prev  # 字节/s
        return {"read_rate": read_rate, "write_rate": write_rate}
    return None

# 获取虚拟机的网络实时速率（字节/s）
def get_network_rate(domain, iface_name):
    iface_stats = domain.interfaceStats(iface_name)
    if iface_stats:
        rx_bytes_prev, tx_bytes_prev = iface_stats[0], iface_stats[4]
        time.sleep(1)  # 等待 1 秒
        iface_stats_new = domain.interfaceStats(iface_name)
        rx_bytes_new, tx_bytes_new = iface_stats_new[0], iface_stats_new[4]
        rx_rate = rx_bytes_new - rx_bytes_prev  # 字节/s
        tx_rate = tx_bytes_new - tx_bytes_prev  # 字节/s
        return {"rx_rate": rx_rate, "tx_rate": tx_rate}
    return None

# 获取虚拟机的磁盘设备名称
def get_disk_devices(domain):
    disk_devices = []
    try:
        xml_desc = domain.XMLDesc(0)
        # 解析 XML 获取磁盘设备名称
        import xml.etree.ElementTree as ET
        root = ET.fromstring(xml_desc)
        for disk in root.findall(".//devices/disk/target[@dev]"):
            disk_devices.append(disk.attrib["dev"])
    except Exception as e:
        print(f"Failed to get disk devices for VM {domain.name()}: {e}")
    return disk_devices

# 获取虚拟机的网络接口名称
def get_network_interfaces(domain):
    iface_names = []
    try:
        xml_desc = domain.XMLDesc(0)
        # 解析 XML 获取网络接口名称
        import xml.etree.ElementTree as ET
        root = ET.fromstring(xml_desc)
        for iface in root.findall(".//devices/interface/target[@dev]"):
            iface_names.append(iface.attrib["dev"])
    except Exception as e:
        print(f"Failed to get network interfaces for VM {domain.name()}: {e}")
    return iface_names

# 更新 Prometheus 指标
def update_metrics(domain, vm_name):
    memory_usage = get_memory_usage(domain)
    cpu_usage = get_cpu_usage(domain)

    if memory_usage is not None:
        memory_usage_gauge.labels(vm_name=vm_name).set(memory_usage)
    if cpu_usage is not None:
        cpu_usage_gauge.labels(vm_name=vm_name).set(cpu_usage)

    # 获取磁盘 IO 速率
    disk_devices = get_disk_devices(domain)
    for disk_device in disk_devices:
        disk_io_rate = get_disk_io_rate(domain, disk_device)
        if disk_io_rate is not None:
            disk_read_rate_gauge.labels(vm_name=vm_name, disk_device=disk_device).set(disk_io_rate["read_rate"])
            disk_write_rate_gauge.labels(vm_name=vm_name, disk_device=disk_device).set(disk_io_rate["write_rate"])

    # 获取网络实时速率
    iface_names = get_network_interfaces(domain)
    for iface_name in iface_names:
        network_rate = get_network_rate(domain, iface_name)
        if network_rate is not None:
            network_rx_rate_gauge.labels(vm_name=vm_name, iface_name=iface_name).set(network_rate["rx_rate"])
            network_tx_rate_gauge.labels(vm_name=vm_name, iface_name=iface_name).set(network_rate["tx_rate"])

# 主函数
def main():
    conn = connect_to_kvm()

    # 启动 Prometheus HTTP 服务器
    start_http_server(8000)  # 暴露指标在 8000 端口

    while True:
        # 获取所有虚拟机
        domains = conn.listAllDomains()
        for domain in domains:
            vm_name = domain.name()
            update_metrics(domain, vm_name)

        time.sleep(5)  # 每 5 秒更新一次指标

if __name__ == "__main__":
    main()