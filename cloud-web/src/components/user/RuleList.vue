<template>
  <div>
    <el-row>
      <el-col :span="24">
        <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
          <el-button icon="el-icon-plus" plain type="primary" @click="on_create_rule_click">添加权限</el-button>&nbsp;
        </div>
      </el-col>
    </el-row>
    <el-divider></el-divider>
    <div style="display: flex">
      <el-table ref="filterTable" :data="all_rules" style="width: 100%">
        <el-table-column
            label="权限ID"
            prop="id"
            width="80">
        </el-table-column>
        <el-table-column
            label="权限名称"
            prop="name"
            width="300">
        </el-table-column>
        <el-table-column
            label="操作">
          <template slot-scope="scope">
            <el-button plain size="small" type="primary" @click="change_rule_click(scope.row)">修改权限</el-button>
            <el-popconfirm style="margin-left: 10px" title="确认删除当前权限组？" @confirm="delete_rule_click(scope.row)">
              <el-button slot="reference" plain size="small" type="danger">删除权限</el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <EditRule ref="EditRuleRef" @on_create="on_rule_create_callback"  @on_modify="on_rule_modify_callback"/>
  </div>
</template>

<script>
import EditRule from "./EditRule";
export default {
  name: "RuleList",
  components: {EditRule },
  data() {
    return {
    }
  },
  created() {
    this.load_permission_category().then(()=>this.load_permission().then(()=> this.load_rules()))
  },
  methods: {
    on_create_rule_click() {
      this.$refs.EditRuleRef.init_data(undefined,this.all_permission_category,this.all_permission)
    },
    on_rule_create_callback(rule) {
      this.all_rules.push(rule)
      this.$emit("on_rule_create", rule)
    },
    on_rule_modify_callback(rule) {
      let findIndex = this.all_rules.findIndex(item => item.id === rule.id)
      this.$set(this.all_rules, findIndex, rule)
      this.$emit("on_rule_modify", rule)
    },
    change_rule_click(rule) {
      this.$refs.EditRuleRef.init_data(rule,this.all_permission_category,this.all_permission)
    },
    delete_rule_click(rule) {
      this.post_data(`/management/rules/destroy`, {id: rule.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_rules.findIndex(item => item.id === rule.id)
          this.$delete(this.all_rules, findIndex)
          this.$emit("on_rule_destroy", rule)
        }
      })
    }
  }
}
</script>

<style scoped>
.main {
  height: 100%;
}

.container {
  height: calc(100% - 60px);;
}
</style>