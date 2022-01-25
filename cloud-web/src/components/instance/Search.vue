<template>
  <div>
    <el-form :inline="true"  class="demo-form-inline"  >
      <el-form-item label="群组" >
        <el-select v-model="search.groupId" size="small" style="width: 100px">
          <el-option :key="-1" :value="-1" label="无限制"/>
          <el-option :key="0" :value="0" label="默认"/>
          <el-option
              v-for="group in all_groups"
              :key="group.id"
              :label="group.name"
              :value="group.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="集群">
        <el-select v-model="search.clusterId"  @change="on_select_cluster_change"  size="small" style="width: 100px">
          <el-option :key="0" :value="0" label="无限制"/>
          <el-option
              v-for="cluster in all_cluster"
              :key="cluster.id"
              :label="cluster.name"
              :value="cluster.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="运行主机">
        <el-select v-model="search.hostId"  size="small" style="width: 100px">
          <el-option :key="0" :value="0" label="无限制"/>
          <el-option
              v-for="host in select_host"
              :key="host.id"
              :label="host.name"
              :value="host.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="实例类型"  >
        <el-select v-model="search.type" size="small" style="width: 100px">
          <el-option key="" value="" label="无限制"/>
          <el-option key="Guest" value="Guest" label="用户"/>
          <el-option key="System" value="System" label="系统"/>
        </el-select>
      </el-form-item>
      <el-form-item label="实例状态"  >
        <el-select v-model="search.status" size="small" style="width: 100px">
          <el-option key="" value="" label="无限制"/>
          <el-option key="Starting" value="Starting" label="Starting"/>
          <el-option key="Creating" value="Creating" label="Creating"/>
          <el-option key="Running" value="Running" label="Running"/>
          <el-option key="Stopped" value="Stopped" label="Stopped"/>
          <el-option key="Destroy" value="Destroy" label="Destroy"/>
          <el-option key="Error" value="Error" label="Error"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="on_search_volume_click" size="small" >查询</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: "Search.vue",
  data(){
    return {
      search:{
        clusterId:0,
        hostId:0,
        groupId:-1,
        type:"Guest",
        status:"",
      },
      search_host_list:[],
    }
  },
  computed:{
    select_host(){
      return this.search_host_list.filter(v=>{
        return v.isShow===undefined || v.isShow
      })
    }
  },
  methods:{
    init_data(all_groups,all_cluster,all_host){
      this.all_groups=all_groups
      this.all_cluster=all_cluster
      this.all_host=all_host
      this.init_search_host()
    },
    init_search_host(){
      this.search_host_list=[]
      let contains=false
      for (let host of this.all_host) {
        if(this.search.clusterId===0||this.search.clusterId===host.clusterId){
          this.search_host_list.push({...host,isShow:true})
        }
        if(this.search.clusterId===0||(host.id===this.search.storageId&&host.clusterId===this.search.clusterId)){
          contains=true
        }
      }
      if(!contains){
        this.search.hostId = 0
      }
    },
    on_select_cluster_change(){
      this.init_search_host()
    },
    on_search_volume_click(){
      this.$emit("on_search", this.search)
    }
  }
}
</script>

<style scoped>

</style>