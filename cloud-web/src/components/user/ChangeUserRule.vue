<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form ref="create_project_service_ref" :model="modify" label-position="right" label-width="80px">
      <el-form-item label="用户权限">
        <el-select v-model="modify.rule" style="width: 200px">
          <el-option
              v-for="rule in all_rules"
              :key="rule.id"
              :label="rule.name"
              :value="rule.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="modify.name==''" @click="modify_user">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "ChangeUserRule",
  data(){
    return {
      modify: {
        userId:  0,
        rule:""
      },
      dialog_visible: false,
      title: ""
    }
  },
  methods:{
    init_data(user,all_rules){
      this.title="修改权限"
      this.modify.userId=user.userId
      this.modify.rule=user.rule
      this.all_rules=all_rules
      this.dialog_visible=true
    },
    modify_user(){
        this.post_data(`/management/user/rule`, this.modify).then(res => {
          if (res.data.code === 0) {
            this.$emit("on_modify", res.data.data)
            this.dialog_visible = false
          }
        })
    }
  }
}
</script>

<style scoped>

</style>