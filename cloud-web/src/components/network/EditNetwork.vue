<template>
  <el-dialog  :title="title" :visible.sync="dialog_visible"  center width="700px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="modify" label-position="right" label-width="100px"  :inline="true" class="demo-form-inline">
      <el-form-item label="名称">
        <el-input v-model="modify.name" style="width: 200px" ></el-input>
      </el-form-item>
      <el-form-item label="网络类型">
        <el-select v-model="modify.type" style="width: 200px">
          <el-option key="Bridge" label="Bridge" value="Bridge">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="网卡名称">
        <el-input v-model="modify.card" style="width: 200px" placeholder="例:br0"></el-input>
      </el-form-item>
      <el-form-item label="集群">
        <el-select v-model="modify.clusterId" style="width: 200px">
          <el-option
              v-for="cluster in all_cluster"
              :key="cluster.id"
              :label="cluster.name"
              :value="cluster.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="管理开始IP">
        <el-input v-model="modify.managerStartIp" style="width: 200px" placeholder="例:192.168.3.2"></el-input>
      </el-form-item>
      <el-form-item label="管理结束IP">
        <el-input v-model="modify.managerEndIp" style="width: 200px"  placeholder="例:192.168.3.20"></el-input>
      </el-form-item>
      <el-form-item label="实例开始IP">
        <el-input v-model="modify.guestStartIp" style="width: 200px"  placeholder="例:192.168.3.100"></el-input>
      </el-form-item>
      <el-form-item label="实例结束IP">
        <el-input v-model="modify.guestEndIp" style="width: 200px"  placeholder="例:192.168.3.100"></el-input>
      </el-form-item>

      <el-form-item label="子网地址">
        <el-input v-model="modify.subnet" style="width: 200px"  placeholder="例:192.168.3.0/24"></el-input>
      </el-form-item>
      <el-form-item label="网关地址">
        <el-input v-model="modify.gateway" style="width: 200px"  placeholder="例:192.168.3.1"></el-input>
      </el-form-item>
      <el-form-item label="系统DNS">
        <el-input v-model="modify.dns" style="width: 200px"  placeholder="例:192.168.3.1,8.8.8.8"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" style="text-align: right">
      <el-button :loading="loading" type="primary" @click="create_storage">确 定</el-button>
      <el-button @click="dialog_visible = false">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "EditNetwork",
  data() {
    return {
      modify: {
        name: "",
        clusterId: "",
        card: "",
        type: "Bridge",
        managerStartIp: "",
        managerEndIp: "",
        guestStartIp: "",
        guestEndIp: "",
        subnet: "",
        gateway: "",
        dns: ""
      },
      dialog_visible: false,
      loading: false,
      title: ""
    }
  },
  methods: {
    init_data(all_cluster) {
      this.title = "创建网络"
      this.modify.name = ""
      this.modify.clusterId = ""
      this.modify.card = ""
      this.modify.type = "Bridge"
      this.modify.managerStartIp = ""
      this.modify.managerEndIp = ""
      this.modify.guestStartIp = ""
      this.modify.guestEndIp = ""
      this.modify.subnet = ""
      this.modify.gateway = ""
      this.modify.dns = ""
      this.loading = false
      this.all_cluster = all_cluster
      this.dialog_visible = true
    },
    create_storage() {
      this.loading = true
      this.post_data(`/management/network/create`, this.modify).then(res => {
        this.loading = false
        if (res.data.code === 0) {
          this.$emit("on_create", res.data.data)
          this.dialog_visible = false
        }
      })
    }
  }
}
</script>

<style scoped>

</style>