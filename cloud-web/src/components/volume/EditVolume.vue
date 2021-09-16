<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form ref="create_scheme_service_ref" :model="modify" label-position="right" label-width="80px">
      <el-form-item label="名称" prop="serviceName">
        <el-input v-model="modify.name"></el-input>
      </el-form-item>

      <el-form-item label="集群">
        <el-select v-model="modify.clusterId" @change="on_cluster_change">
          <el-option
              v-for="cluster in all_cluster"
              :key="cluster.id"
              :label="cluster.name"
              :value="cluster.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="存储池">
        <el-select v-model="modify.storageId">
          <el-option :value="0" label="无限制"></el-option>
          <el-option
              v-for="storage in search_storage"
              :key="storage.id"
              :label="storage.name"
              :value="storage.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="磁盘大小" prop="size">
        <el-input-number v-model="modify.size" :max="10000" :min="10"></el-input-number>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="modify.name==''||modify.size===0" @click="crate_volume">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "EditVolume",
  data(){
    return {
      modify: {
        name:"",
        clusterId: "",
        storageId: 0,
        size: 10,
      },
      dialog_visible: false,
      title: ""
    }
  },
  computed:{
    search_storage(){
      return this.all_storage.filter(v=>v.isShow)
    }
  },
  methods:{
    init_data(all_cluster,all_storage){
      this.title="创建磁盘"
      this.all_cluster=all_cluster.map(item => ({...item,isShow:true}))
      this.all_storage=all_storage.map(item => ({...item,isShow:false}))
      this.modify.storageId=0
      this.modify.size=100
      this.modify.name=""
      this.modify.clusterId=""

      this.dialog_visible=true
    },
    on_cluster_change(clusterId){
      this.all_storage.forEach((item,index) => {
         item.isShow=item.clusterId===clusterId
         this.$set(this.all_volume, index, item)
      })
    },
    crate_volume(){
        this.post_data(`/management/volume/create`, this.modify).then(res => {
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