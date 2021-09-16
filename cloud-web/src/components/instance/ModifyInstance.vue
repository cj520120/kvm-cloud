<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="modify" label-position="right" label-width="90px">
      <el-form-item label="名称">
        <el-input v-model="modify.description"></el-input>
      </el-form-item>
      <el-form-item label="集群">
        <el-select v-model="modify.groupId" size="small" style="width: 100px">
          <el-option :key="0" :value="0" label="默认"/>
          <el-option
              v-for="group in all_groups"
              :key="group.id"
              :label="group.name"
              :value="group.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="计算方案">
      <el-select v-model="modify.calculationSchemeId" size="small" style="width: 100px">
        <el-option
            v-for="scheme in all_scheme"
            :key="scheme.id"
            :label="scheme.name"
            :value="scheme.id">
        </el-option>
      </el-select>
    </el-form-item>
      <el-form-item>
        <el-button :loading="loading" type="primary" :disabled="modify.name==''" @click="ok">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>

  </el-dialog>
</template>

<script>
export default {
  name: "ModifyInstance",
  data(){
    return {
      modify:{
        id:0,
        description:"",
        groupId:0,
        calculationSchemeId:0
      },
      title:"",
      loading:false,
      dialog_visible:false
    }
  },
  methods:{
    init_data(instance,all_scheme,all_groups){
      this.title=`${instance.description}-修改`
      this.all_scheme=all_scheme
      this.all_groups=all_groups
      this.dialog_visible=true
      this.loading=false
      this.modify.id=instance.id
      this.modify.description=instance.description
      this.modify.groupId=instance.groupId
      this.modify.calculationSchemeId=instance.calculationSchemeId
    },
    ok(){
      this.loading=true
      this.post_data(`/management/vm/modify`, this.modify).then(res => {
        this.loading=false
        if (res.data.code === 0) {
          this.$emit("on_modify", res.data.data)
          this.dialog_visible = false
        }
      })
      this.dialog_visible=true
    }
  }
}
</script>

<style scoped>

</style>