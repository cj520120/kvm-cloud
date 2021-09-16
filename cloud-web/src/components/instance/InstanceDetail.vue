<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="600px"  height="400px" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-tabs v-model="activeName">
      <el-tab-pane label="基础信息" name="first">
          <div class="details" style="width: 600px;height:400px;">
            <el-row :gutter="20">
              <el-col :span="3"><div class="title">实例编号</div></el-col>
              <el-col :span="17"><div class="content">{{instance.id}}</div></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="3"><div class="title">实例名称</div></el-col>
              <el-col :span="17"><div class="content">{{instance.name}}</div></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="3"><div class="title">实例备注</div></el-col>
              <el-col :span="17"><div class="content">{{instance.description}}</div></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="3"><div class="title"> 集群名称</div></el-col>
              <el-col :span="17"><div class="content">{{get_cluster_name_by_id(instance.clusterId)}}</div></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="3"><div class="title">所属群组</div></el-col>
              <el-col :span="17"><div class="content">{{get_group_name_by_id(instance.groupId)}}</div></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="3"><div class="title">实例类型</div></el-col>
              <el-col :span="17"><div class="content">  <el-tag  size="mini">{{instance.type}}</el-tag> </div></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="3"><div class="title">运行状态</div></el-col>
              <el-col :span="17"><div class="content"><el-tag :type="get_status_tag_type(instance.status)" size="mini">{{ instance.status }}</el-tag></div></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="3"><div class="title"> 计算方案</div></el-col>
              <el-col :span="17"><div class="content">{{get_scheme_name_by_id(instance.calculationSchemeId)}}</div></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="3"><div class="title">运行主机</div></el-col>
              <el-col :span="17"><div class="content">{{get_host_name_by_id(instance.hostId)}}</div></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="3"><div class="title">附加光盘</div></el-col>
              <el-col :span="17"><div class="content">{{get_template_name_by_id(instance.iso)}}</div></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="3"><div class="title"> 系统模版</div></el-col>
              <el-col :span="17"><div class="content">{{get_template_name_by_id(instance.templateId)}}</div></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="3"><div class="title">创建时间</div></el-col>
              <el-col :span="17"><div class="content">{{ parse_date(instance.createTime) }}</div></el-col>
            </el-row>
          </div>
      </el-tab-pane>
      <el-tab-pane label="磁盘信息" name="second">
        <div style="width: 600px;height:400px;overflow: auto">
          <el-table   v-loading="volume_loading" size="small"  :data="instance_volumes">
            <el-table-column
                label="ID"
                prop="id"
                width="60">
            </el-table-column>
            <el-table-column
                label="名称"
                prop="name"
                width="130">
            </el-table-column>
            <el-table-column
                label="类型"
                prop="type"
                width="90">
              <template slot-scope="scope">
                <el-tag>{{ scope.row.type===0?"ROOT":"DATA" }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column
                label="状态"
                prop="status"
                width="100">
              <template slot-scope="scope">
                <el-tag :type="get_status_tag_type(scope.row.status)">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column
                label="创建时间"
                prop="createTime"
                width="150"
                >
              <template slot-scope="scope">
                <span>{{ parse_date(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane label="网络信息" name="third">
        <div  style="width: 600px;height:400px;overflow: auto">
          <el-table   v-loading="network_loading" size="small"  ref="filterTable" :data="instance_network_interfaces" >
            <el-table-column
                label="ID"
                prop="id"
                width="60">
            </el-table-column>
            <el-table-column
                label="MAC"
                prop="mac"
                width="140">
            </el-table-column>
            <el-table-column
                label="IP"
                prop="ip"
                width="120">
            </el-table-column>
            <el-table-column
                label="创建时间"
                prop="createTime"
                width="150">
              <template slot-scope="scope">
                <span>{{ parse_date(scope.row.createTime) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="操作">
              <template slot-scope="scope">
                <el-popconfirm title="确认卸载当前网卡？" style="margin-left: 10px"  :disabled="scope.row.device===0" @confirm="on_detach_network_interface_click(scope.row)">
                  <el-button slot="reference" plain size="mini" type="danger" :disabled="scope.row.device===0">卸载</el-button>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

  </el-dialog>
</template>

<script>
export default {
  name: "InstanceDetail",
  data(){
    return {
      id:0,
      instance:{},
      activeName:"first",
      title:"",
      instance_volumes:[],
      instance_network_interfaces:[],
      dialog_visible:false,
      volume_loading:false,
      network_loading:false,
    }
  },
  methods:{
    init_data(instance,all_cluster,all_groups,all_host,all_scheme,all_template){
      this.instance=instance
      this.id=instance.id
      this.title=`${instance.description}-详情`
      this.all_cluster=all_cluster
      this.all_groups=all_groups
      this.all_host=all_host
      this.all_scheme=all_scheme
      this.all_template=all_template
      this.instance_volumes=[]
      this.instance_network_interfaces=[]
      this.volume_loading=false
      this.network_loading=false
      this.dialog_visible=true
      this.load_instance_volume()
      this.load_network_interface()
    },
    get_status_tag_type(status){
      return status === 'Ready' ? '' : 'danger'
    },
    load_instance_volume(){
      this.volume_loading=true
      this.axios_get(`/management/volume/search?clusterId=0&vmId=${this.id}&storageId=0`).then(res=>{
        this.volume_loading=false
        if(res.data.code===0&&res.data.data){
          this.instance_volumes=res.data.data
        }
      })
    },
    load_network_interface(){
      this.network_loading=true
      this.axios_get(`/management/network/vm?vmId=${this.id}`).then(res=>{
        this.network_loading=false
        if(res.data.code===0&&res.data.data){
          this.instance_network_interfaces=res.data.data
        }
      })
    },
    on_detach_network_interface_click(network){
      this.post_data(`/management/vm/detach/network`, {vmId:this.id,vmNetworkId:network.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.instance_network_interfaces.findIndex(item => item.id === network.id)
          this.$delete(this.instance_network_interfaces,findIndex)
        }
      })
    }
  }
}
</script>

<style scoped>
.details .title{
  color: #909399;
  font-size: 12px;
  line-height: 32px;
  width: 100%;

}
.details .content{
  color: #606266;
  font-size: 12px;
  line-height: 32px;
}
</style>