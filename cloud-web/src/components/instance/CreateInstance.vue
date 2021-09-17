<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="600px"  :close-on-click-modal="false" :close-on-press-escape="false">
   <el-container>
    <el-header>
      <el-steps :active="active" finish-status="success" :simple="false" :align-center="true">
        <el-step title="基本信息"></el-step>
        <el-step title="系统镜像"></el-step>
        <el-step title="网络与存储"></el-step>
        <el-step title="完成"></el-step>
      </el-steps>
    </el-header>
     <el-divider></el-divider>
    <el-main>
      <el-form :model="modify" v-show="active===0" label-position="right" label-width="90px">
        <el-form-item label="名称">
          <el-input v-model="modify.name" style="width:200px"></el-input>
        </el-form-item>
        <el-form-item label="集群">
          <el-select v-model="modify.clusterId" @change="on_select_change">
            <el-option
                v-for="cluster in all_cluster"
                :key="cluster.id"
                :label="cluster.name"
                :value="cluster.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="系统类型">
          <el-select v-model="modify.templateType" @change="on_select_change">
            <el-option  key="ISO" label="ISO镜像" value="ISO"></el-option>
            <el-option  key="Disk" label="系统模版" value="Disk"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </el-main>
   </el-container>
    <el-form :model="modify" v-show="active===1" label-position="right" label-width="90px">
      <el-form-item label="选择系统">
        <el-select v-model="modify.templateId" >
          <el-option
              v-for="template in select_template"
              :key="template.id"
              :label="template.name"
              :value="template.id">
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <el-form :model="modify" v-show="active===2" label-position="right" label-width="90px">
      <el-form-item label="系统网络">
        <el-select v-model="modify.networkId" >
          <el-option
              v-for="network in select_network"
              :key="network.id"
              :label="network.name"
              :value="network.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="存储池">
        <el-select v-model="modify.storageId" >
          <el-option  :key="0" label="不限制" :value="0"></el-option>
          <el-option
              v-for="storage in select_storage"
              :key="storage.id"
              :label="storage.name"
              :value="storage.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="磁盘大小" prop="size">
        <el-input-number v-model="modify.size" :max="10000" :min="10"></el-input-number>
      </el-form-item>
    </el-form>


    <el-form :model="modify" v-show="active===3" label-position="right" label-width="90px">

      <el-form-item label="分组">
        <el-select v-model="modify.groupId">
          <el-option  :key="0" label="默认" :value="0"></el-option>
          <el-option
              v-for="group in all_groups"
              :key="group.id"
              :label="group.name"
              :value="group.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="计算方案">
        <el-select v-model="modify.calculationSchemeId" >
          <el-option
              v-for="scheme in all_scheme"
              :key="scheme.id"
              :label="scheme.name"
              :value="scheme.id">
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <div slot="footer" style="text-align: right">
      <el-button type="primary" v-show="active>0" @click="prev">上一步</el-button>
      <el-button type="primary" v-show="active===0" :disabled="modify.name==='' || modify.clusterId==='' " @click="next">下一步</el-button>
      <el-button type="primary" v-show="active===1" :disabled="modify.templateId==='' || modify.size===''" @click="next">下一步</el-button>
      <el-button type="primary" v-show="active===2" :disabled="modify.networkId==='' " @click="next">下一步</el-button>
      <el-button :loading="loading" type="primary" v-show="active===3" @click="create_instance">完成</el-button>
      <el-button @click="dialog_visible = false">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "CreateInstance",
  data(){
    return {
      modify:{
        name:"",
        groupId:0,
        clusterId:"",
        templateType:"",
        templateId:"",
        networkId:"",
        storageId:"",
        calculationSchemeId:0,
        size:100
      },
      active:0,
      title:"",
      loading:false,
      dialog_visible:false
    }
  },
  computed:{
    select_template(){
      return this.all_template.filter(v=>{
        return v.isShow===undefined || v.isShow
      })
    },
    select_network(){
      return this.all_network.filter(v=>{
        return v.isShow===undefined || v.isShow
      })
    },
    select_storage(){
      return this.all_storage.filter(v=>{
        return v.isShow===undefined || v.isShow
      })
    }
  },
  methods:{
    init_data(all_cluster,all_groups,all_scheme,all_template,all_storage,all_network){
      this.title='创建实例'
      this.all_cluster=all_cluster.map(item => ({...item, isShow: true}))
      this.all_groups=all_groups.map(item => ({...item, isShow: true}))
      this.all_scheme=all_scheme.map(item => ({...item, isShow: true}))
      this.all_template=all_template.map(item => ({...item, isShow: false}))
      this.all_storage=all_storage.map(item => ({...item, isShow: false}))
      this.all_network=all_network.map(item => ({...item, isShow: false}))
      this.active=0
      this.modify.name=""
      this.modify.clusterId=""
      this.modify.templateId=""
      this.modify.networkId=""
      this.modify.groupId=0
      this.modify.storageId=0
      this.modify.calculationSchemeId=0
      this.modify.size=100
      this.modify.templateType="ISO"
      this.loading=false
      this.dialog_visible=true
    },
    next() {
      this.active++
    },
    prev() {
      this.active--
    },
    on_select_change(){
      this.all_template.forEach((item,idx)=>{
          if(item.clusterId!=this.modify.clusterId){
            item.isShow=false
          }else if(item.type===this.modify.templateType){
            item.isShow=true
          }else{
            item.isShow=false
          }
          this.$set(this.all_template,item,idx)
      })
      this.all_network.forEach((item,idx)=>{
        if(item.clusterId!=this.modify.clusterId){
          item.isShow=false
        }else{
          item.isShow=true
        }
        console.log(item)
        this.$set(this.all_network,item,idx)
      })
      this.all_storage.forEach((item,idx)=>{
        if(item.clusterId!=this.modify.clusterId){
          item.isShow=false
        }else{
          item.isShow=true
        }
        this.$set(this.all_storage,item,idx)
      })
    },
    create_instance(){
      this.loading=true
      this.post_data(`/management/vm/create`, this.modify).then(res => {
        this.loading=false
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