<template>
  <div class="main">
    <Top/>
    <div style="display: flex"  class="container">
      <Menu :index="menuIndex"/>
      <el-main>
        <el-tabs>
          <el-tab-pane label="磁盘管理">
            <el-row>
              <el-col :span="20">
                <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
                  <el-button icon="el-icon-plus" plain type="primary" @click="on_volume_click">创建磁盘</el-button>&nbsp;
                </div>
              </el-col>
              <el-col :span="4">
                <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
                  <el-input v-model="filter.key" placeholder="请输入关键字" @input="on_filter_key_change"></el-input>
                </div>
              </el-col>
            </el-row>
            <el-divider></el-divider>

            <div>
              <el-form :inline="true"  class="demo-form-inline">
                <el-form-item label="集群">
                  <el-select v-model="search.clusterId"  @change="on_select_cluster_change">
                    <el-option :key="0" :value="0" label="无限制"/>
                    <el-option
                        v-for="cluster in all_cluster"
                        :key="cluster.id"
                        :label="cluster.name"
                        :value="cluster.id">
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="存储池">
                  <el-select v-model="search.storageId">
                    <el-option :key="0" :value="0" label="无限制"/>
                    <el-option
                        v-for="storage in select_storage"
                        :key="storage.id"
                        :label="storage.name"
                        :value="storage.id">
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="实例">
                  <el-select v-model="search.vmId">
                    <el-option :key="-1" :value="-1" label="无限制"/>
                    <el-option
                        v-for="instance in select_instance"
                        :key="instance.id"
                        :label="instance.description"
                        :value="instance.id">
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="on_search_volume_click">查询</el-button>
                </el-form-item>
              </el-form>
            </div>
            <div style="display: flex">
              <el-table :data="table_volume" style="width: 100%" v-loading="loading" :row-key="get_volume_id" :expand-row-keys="expand_volumes">
                <el-table-column type="expand">
                  <template slot-scope="props">
                    <div>
                      <el-form label-position="left" inline class="demo-table-expand">
                        <el-form-item label="编号">
                          <span>{{ props.row.id }}</span>
                        </el-form-item>
                        <el-form-item label="名称">
                          <span>{{ props.row.name }}</span>
                        </el-form-item>
                        <el-form-item label="集群">
                          <span>{{ get_cluster_name_by_id(props.row.clusterId) }}</span>
                        </el-form-item>
                        <el-form-item label="存储池">
                          <span>{{ get_storage_name_by_id(props.row.storageId) }}</span>
                        </el-form-item>
                        <el-form-item label="所属实例">
                          <span>{{ get_instance_name_by_id(props.row.vmId) }}</span>
                        </el-form-item>
                        <el-form-item label="挂载路径">
                          <span>{{ get_volume_path(props.row) }}</span>
                        </el-form-item>
                        <el-form-item label="磁盘容量">
                          <span>{{ parse_disk_capacity(props.row.capacity) }}</span>
                        </el-form-item>
                        <el-form-item label="物理大小">
                          <span>{{ parse_disk_capacity(props.row.allocation) }}</span>
                        </el-form-item>
                        <el-form-item label="磁盘类型">
                          <el-tag>{{ props.row.device === 0 && props.row.vmId > 0 ? "ROOT" : "DATA" }}</el-tag>
                        </el-form-item>
                        <el-form-item label="磁盘状态">
                          <el-tag  :type="get_volume_status_tag_type(props.row.status)">{{ props.row.status }}</el-tag>
                        </el-form-item>
                        <el-form-item label="创建时间">
                          <span>{{ parse_date(props.row.createTime) }}</span>
                        </el-form-item>
                      </el-form>
                      <div>
                          <el-button plain size="small" type="primary" @click="resize_volume_click(props.row)" v-show="props.row.status==='Ready'">扩容磁盘</el-button>
                          <el-button plain size="small" type="primary" @click="attach_volume_click(props.row)" v-show="props.row.status != `Destroy` && props.row.vmId === 0">挂载磁盘</el-button>
                          <el-popconfirm title="确认卸载当前磁盘？" style="margin-left: 10px" @confirm="detach_volume_click(props.row)" v-show="props.row.status != `Destroy` && props.row.vmId > 0 && props.row.device>0">
                            <el-button slot="reference" plain size="mini" type="danger" >卸载磁盘</el-button>
                          </el-popconfirm>
                          <el-button plain size="small" @click="resume_volume_click(props.row)" type="primary" v-show="props.row.status === 'Destroy'">恢复磁盘</el-button>
                          <el-popconfirm title="确认删除当前磁盘？" style="margin-left: 10px"  @confirm="on_delete_volume_click(props.row)" v-show="props.row.status != 'Destroy' && props.row.vmId === 0">
                            <el-button slot="reference" plain size="mini" type="danger" >销毁磁盘</el-button>
                          </el-popconfirm>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column
                    label="ID"
                    prop="id"
                    width="80">
                </el-table-column>
                <el-table-column
                    label="名称"
                    prop="name"
                    width="200">
                </el-table-column>
                <el-table-column
                    label="集群"
                    prop="clusterId"
                    width="200">
                  <template slot-scope="scope">
                    <span>{{ get_cluster_name_by_id(scope.row.clusterId)}}</span>
                  </template>
                </el-table-column>
                <el-table-column
                    label="存储"
                    prop="storageId"
                    width="200">
                  <template slot-scope="scope">
                    <span>{{ get_storage_name_by_id(scope.row.storageId)}}</span>
                  </template>
                </el-table-column>
                <el-table-column
                    label="挂载实例"
                    prop="vmId"
                    width="200">
                  <template slot-scope="scope">
                    <span>{{ get_instance_name_by_id(scope.row.vmId)}}</span>
                  </template>
                </el-table-column>
                <el-table-column
                    label="类型"
                    width="200">
                  <template slot-scope="scope">
                    <el-tag >{{ scope.row.device === 0 && scope.row.vmId > 0 ? "ROOT" : "DATA" }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column
                    label="状态"
                    prop="status"
                    width="200">
                  <template slot-scope="scope">
                    <el-tag :type="get_volume_status_tag_type(scope.row.status)">{{ scope.row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column
                    label="创建时间"
                    prop="createTime"
                    width="200">
                  <template slot-scope="scope">
                    <span>{{ parse_date(scope.row.createTime) }}</span>
                  </template>
                </el-table-column>

              </el-table>

            </div>

            <div class="block">
              <el-pagination
                  @size-change="page_size_change"
                  @current-change="current_page_change"
                  :current-page="current_page"
                  :page-sizes="[10,20,50,100, 200]"
                  :page-size="page_size"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="total_size">
              </el-pagination>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </div>
    <ResizeVolume ref="ResizeVolumeRef" @on_resize="on_resize_callback"/>
    <AttachVolume ref="AttachVolumeRef" @on_attach="on_attach_callback"/>
    <EditVolume ref="EditVolumeRef" @on_create="on_volume_create_callback"/>
  </div>
</template>

<script>
import Top from "../common/Top";
import Menu from "../common/Menu"
import EditVolume from "./EditVolume";
import ResizeVolume from "./ResizeVolume";
import AttachVolume from "./AttachVolume";
export default {
  name: "Volume.vue",
  components:{Top,Menu,EditVolume,ResizeVolume,AttachVolume},
  data(){
    return {
      menuIndex:"7",
      filter: {
        key: ""
      },
      search:{
        clusterId:0,
        storageId:0,
        vmId:-1,
      },
      loading:true,
      search_storage_list:[],
      search_instance_list:[],
      expand_volumes:[],
      current_page:1,
      page_size:10,
      total_size:0
    }
  },
  mounted() {
    this.on_search_volume_click()
  },
  computed:{
    table_volume(){
      return this.all_volume.filter(v=>{
        return v.isShow===undefined || v.isShow
      })
    },
    select_storage(){
      return this.search_storage_list.filter(v=>{
        return v.isShow===undefined || v.isShow
      })
    },
    select_instance(){
      return this.search_instance_list.filter(v=>{
        return v.isShow===undefined || v.isShow
      })
    }
  },
  methods:{
    current_page_change(current_page){
      this.current_page=current_page
      this.on_filter_key_change()
    },
    on_select_cluster_change(){
      this.init_search_storage()
      this.init_search_instance()
    },
    init_search_storage(){
      this.search_storage_list=[]
      let contains=false
      for (let storage of this.all_storage) {
        if(this.search.clusterId===0||this.search.clusterId===storage.clusterId){
          this.search_storage_list.push({...storage,isShow:true})
        }
        if(this.search.clusterId===0||(storage.id===this.search.storageId&&storage.clusterId===this.search.clusterId)){
          contains=true
        }
      }
      if(!contains){
        this.search.storageId = 0
      }
    },
    init_search_instance(){
      this.search_instance_list=[]
      let contains=false
      for (let instance of this.all_instance) {
        if(this.search.clusterId===0||this.search.clusterId===instance.clusterId){
          this.search_instance_list.push({...instance,isShow:true})
        }
        if(this.search.clusterId===0||(instance.id===this.search.vmId&&instance.clusterId===this.search.clusterId)){
          contains=true
        }
      }
      if(!contains){
        this.search.vmId = -1
      }
    },
    on_search_volume_click(){
      this.loading=true
      this.load_cluster().then(()=>this.load_storage().then(()=>this.load_instance().then(()=>this.search_volume().then(()=>{
        this.current_page=1
        this.on_filter_key_change()
        this.init_search_storage()
        this.init_search_instance()
        this.loading=false
      }))))
    },
    search_volume(){
        return this.axios_get(`/management/volume/search?vmId=${this.search.vmId}&clusterId=${this.search.clusterId}&storageId=${this.search.storageId}`).then(res=>{
          if(res.data.code===0&&res.data.data){
            this.all_volume=res.data.data
          }
        })
    },
    page_size_change(page_size){
      this.page_size=page_size
      this.on_filter_key_change()
    },
    on_volume_click(){
      this.$refs.EditVolumeRef.init_data(this.all_cluster,this.all_storage)
    },
    get_volume_id(volume){
        return volume.id
    },

    on_filter_key_change() {
      let filter = this.filter.key.trim().toLowerCase()
      let nCount=0
      this.all_volume.forEach((item,index) => {
        if(filter=== ''){
          item.isShow=true
        }else{
          const clusterName=this.get_cluster_name_by_id(item.clusterId).toLowerCase()
          const storageName=this.get_cluster_name_by_id(item.storateId).toLowerCase()
          const instanceName=this.get_instance_name_by_id(item.vmId).toLowerCase()
          const volumeName=item.name.toLowerCase()
          item.isShow=clusterName.indexOf(filter)>=0
              ||storageName.indexOf(filter)>=0
              ||instanceName.indexOf(filter)>=0
              ||volumeName.indexOf(filter)>=0
        }
        if(item.isShow){
           nCount++
        }
        if(nCount <= this.page_size*(this.current_page-1)
            || nCount>this.page_size*this.current_page){
          item.isShow=false
        }
        this.$set(this.all_volume, index, item)
      })
      this.total_size=nCount
    },
    on_delete_volume_click(volume){
      this.post_data(`/management/volume/destroy`, {id:volume.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_volume.findIndex(item => item.id === volume.id)
          this.$set(this.all_volume, findIndex, res.data.data)
        }
      })
    },
    on_volume_create_callback(volume){
      this.all_volume.push(volume)
    },
    on_resize_callback(volume){
      let findIndex = this.all_volume.findIndex(item => item.id === volume.id)
      this.$set(this.all_volume, findIndex,volume)
    },
    on_attach_callback(volume){
      let findIndex = this.all_volume.findIndex(item => item.id === volume.id)
      this.$set(this.all_volume, findIndex,volume)
    },
    resize_volume_click(volume){
      this.$refs.ResizeVolumeRef.init_data(volume.id)
    },
    attach_volume_click(volume){
      this.$refs.AttachVolumeRef.init_data(volume.id,this.all_instance)
    },
    get_volume_status_tag_type(status){
      return status ==='Ready'?'':'danger'
    },
    detach_volume_click(volume){
      this.post_data(`/management/vm/detach/disk`, {id:volume.vmId,volume:volume.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_volume.findIndex(item => item.id === volume.id)
          this.$set(this.all_volume, findIndex,res.data.data)
        }
      })
    },
    resume_volume_click(volume){
      this.post_data(`/management/volume/resume`, {id:volume.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_volume.findIndex(item => item.id === volume.id)
          this.$set(this.all_volume, findIndex,res.data.data)
        }
      })
    }
  }
}
</script>

<style>
.main{
  height: 100%;
}
.container{
  height: calc(100%  - 60px);;
}
.demo-table-expand {
  font-size: 0;
}
.demo-table-expand label {
  width: 90px;
  color: #99a9bf;
}
.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 100%;
}
</style>