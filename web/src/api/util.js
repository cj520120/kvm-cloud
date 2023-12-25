export default {
  methods: {
    get_bridge_type(network) {
      switch (network.bridgeType) {
        case 0:
          return "基础桥接";
        case 1:
          return "OpenSwitch";
        default:
          return `未知桥接[${network.bridgeType}]`;
      }
    },
    get_guest_status(guest) {
      switch (guest.status) {
        case 0:
          return "正在创建";
        case 1:
          return "正在启动";
        case 2:
          return "正在运行";
        case 3:
          return "正在停止";
        case 4:
          return "已停止";
        case 5:
          return "重启中";
        case 6:
          return "虚拟机错误";
        case 7:
          return "正在迁移";
        case 8:
          return "正在销毁";
        default:
          return `未知状态[${guest.status}]`;
      }
    },
    get_host_status(host) {
      switch (host.status) {
        case 0:
          return "正在创建";
        case 1:
          return "在线";
        case 2:
          return "离线";
        case 3:
          return "正在维护";
        case 4:
          return "主机错误";
        default:
          return `未知状态[${host.status}]`;
      }
    },
    get_network_status(network) {
      switch (network.status) {
        case 1:
          return "正在注册";
        case 2:
          return "已就绪";
        case 3:
          return "正在维护";
        case 4:
          return "正在销毁";
        case 5:
          return "网络错误";
        default:
          return `未知状态[${network.status}]`;
      }
    },
    get_network_type(network) {
      switch (network.type) {
        case 0:
          return "基础网络";
        case 1:
          return "Vlan网络";
        default:
          return `未知类型[${network.type}]`;
      }
    },
    get_snapshot_status(snapshot) {
      switch (snapshot.status) {
        case 0:
          return "正在创建";
        case 1:
          return "已就绪";
        case 2:
          return "快照错误";
        case 3:
          return "正在删除";
        default:
          return `未知状态[${snapshot.status}]`;
      }
    },
    get_storage_status(storage) {
      switch (storage.status) {
        case 0:
          return "正在创建";
        case 1:
          return "已就绪";
        case 2:
          return "正在维护";
        case 3:
          return "正在销毁";
        case 4:
          return "存储池错误";
        default:
          return `未知状态[${storage.status}]`;
      }
    },
    get_memory_display_size(memory) {
      if (memory >= 1024 * 1024) {
        return (memory / (1024 * 1024)).toFixed(2) + " GB";
      } else if (memory >= 1024) {
        return (memory / 1024).toFixed(2) + "  MB";
      } else {
        return memory + " KB";
      }
    },

    get_template_status(template) {
      switch (template.status) {
        case 0:
          return "正在创建";
        case 1:
          return "下载中";
        case 2:
          return "已就绪";
        case 3:
          return "模版错误";
        case 4:
          return "正在销毁";
        default:
          return `未知状态[${template.status}]`;
      }
    },
    get_template_type(template) {
      switch (template.templateType) {
        case 0:
          return "IOS 文件";
        case 1:
          return "系统模版";
        case 2:
          return "用户模版";
        default:
          return `未知模版[${template.templateType}]`;
      }
    },
    get_volume_status(volume) {
      switch (volume.status) {
        case 0:
          return "正在创建";
        case 1:
          return "已就绪";
        case 2:
          return "正在挂载";
        case 3:
          return "正在卸载";
        case 4:
          return "正在克隆";
        case 5:
          return "创建模版";
        case 6:
          return "创建快照";
        case 7:
          return "正在迁移";
        case 8:
          return "正在扩容";
        case 9:
          return "正在销毁";
        case 10:
          return "磁盘错误";
        default:
          return `未知状态[${volume.status}]`;
      }
    },
    get_component_type(componentType) {
      switch (componentType) {
        case 1:
          return "Route";
        case 2:
          return "Nat";
        default:
          return "未知";
      }
    },
    get_volume_display_size(size) {
      if (size >= 1024 * 1024 * 1024 * 1024) {
        return (size / (1024 * 1024 * 1024 * 1024)).toFixed(2) + " TB";
      } else if (size >= 1024 * 1024 * 1024) {
        return (size / (1024 * 1024 * 1024)).toFixed(2) + " GB";
      } else if (size >= 1024 * 1024) {
        return (size / (1024 * 1024)).toFixed(2) + " MB";
      } else if (size >= 1024) {
        return (size / 1024).toFixed(2) + "  KB";
      } else {
        return size + "  bytes";
      }
    },
    parse_date(date, fmt = "yyyy-MM-dd hh:mm:ss") {
      if (!date) {
        return "";
      }
      date = new Date(Number(date));
      let o = {
        "M+": date.getMonth() + 1, // 月份
        "d+": date.getDate(), // 日
        "h+": date.getHours(), // 小时
        "m+": date.getMinutes(), // 分
        "s+": date.getSeconds(), // 秒
        "q+": Math.floor((date.getMonth() + 3) / 3), // 季度
        S: date.getMilliseconds(), // 毫秒
      };
      if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(
          RegExp.$1,
          (date.getFullYear() + "").substr(4 - RegExp.$1.length)
        );
      }
      for (let k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
          fmt = fmt.replace(
            RegExp.$1,
            RegExp.$1.length === 1
              ? o[k]
              : ("00" + o[k]).substr(("" + o[k]).length)
          );
        }
      }
      return fmt;
    },
  },
};
