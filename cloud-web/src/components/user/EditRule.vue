<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form ref="create_project_service_ref" :model="modify" label-position="right" label-width="80px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="modify.name"></el-input>
      </el-form-item>

      <el-form-item label="权限" prop="permissions">
        <div style="height: 300px;overflow: auto;border: 1px solid #DCDFE6" >
        <el-tree
            :data="permission_tree"
            @check="on_permission_tree_change"
            show-checkbox
            node-key="id"
            default-expand-all
            :default-checked-keys="default_check_permissions_list"
            :expand-on-click-node="false">
        </el-tree></div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="modify.name==''" @click="modify_rule">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "EditRule",
  data(){
    return {
      modify: {
        id: 0,
        name: "",
        permissions:""
      },
      default_check_permissions_list:[],
      permission_tree:[],
      dialog_visible: false,
      title: ""
    }
  },
  methods:{
    init_data(rule,all_permission_category,all_permission){
      if(rule){
        this.title="编辑群组"
        this.modify.id=rule.id
        this.modify.name=rule.name
        this.modify.permissions=rule.permissions.join(",")
        this.default_check_permissions_list=rule.permissions
      }else{
        this.title="创建群组"
        this.modify.id=0
        this.modify.name=""
        this.modify.permissions=""
        this.default_check_permissions_list=[]
      }
      this.permission_tree=[]
      for (let idx in all_permission_category) {
          const category=all_permission_category[idx]
          const node={
            id:`category_${category.id}`,
            label:category.name,
            children:[]
          }
          for(let idx in all_permission){
            const permission=all_permission[idx]
            if (permission.categoryId===category.id){
              node.children.push({
                id:permission.id,
                label:permission.description,
              })
            }
          }
          this.permission_tree.push(node)

      }
      this.dialog_visible=true
    },
    on_permission_tree_change(data,node){
      this.default_check_permissions_list=node.checkedKeys.filter(v=>typeof v === 'number')
      this.modify.permissions=this.default_check_permissions_list.join(",")

    },
    modify_rule(){
      if (this.modify.id === 0) {
        this.post_data(`/management/rules/create`, this.modify).then(res => {
          if (res.data.code === 0) {
            this.$emit("on_create", res.data.data)
            this.dialog_visible = false
          }
        })
      }else {
        this.post_data(`/management/rules/modify`, this.modify).then(res => {
          if (res.data.code === 0) {
            this.$emit("on_modify", res.data.data)
            this.dialog_visible = false
          }
        })
      }
    }
  }
}
</script>

<style scoped>

</style>