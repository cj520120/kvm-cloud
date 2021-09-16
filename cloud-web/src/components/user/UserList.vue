<template>
  <div>
    <el-row>
      <el-col :span="24">
        <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
          <el-button icon="el-icon-plus" plain type="primary" @click="on_register_user_click">注册用户</el-button>&nbsp;
        </div>
      </el-col>
    </el-row>
    <el-divider></el-divider>
    <div style="display: flex">
      <el-table ref="filterTable" :data="all_users" style="width: 100%">
        <el-table-column
            label="用户ID"
            prop="userId"
            width="80">
        </el-table-column>
        <el-table-column
            label="登录名"
            prop="loginName"
            width="100">
        </el-table-column>
        <el-table-column
            label="权限"
            prop="rule"
            width="200">
          <template slot-scope="scope">
            <el-tag>{{ get_rule_name_by_id(scope.row.rule) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
            label="状态"
            prop="state"
            width="100">
          <template slot-scope="scope">
            <el-tag>{{ scope.row.state === 0 ? "启用" : "禁用" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
            label="注册时间"
            prop="createTime"
            width="200">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ parse_date(scope.row.registerTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column
            label="操作">
          <template slot-scope="scope">
            <el-button plain size="small" type="primary" @click="reset_password_click(scope.row)">重置密码</el-button>
            <el-button plain size="small" type="primary" @click="change_rule_click(scope.row)">修改权限</el-button>
            <el-button v-show="scope.row.state!==0" plain size="small" type="primary" @click="enable_user_click(scope.row)">启用用户</el-button>
            <el-button v-show="scope.row.state===0" plain size="small" type="danger" @click="disable_user_click(scope.row)">禁用用户</el-button>
            <el-popconfirm style="margin-left: 10px" title="确认删除当前用户？" @confirm="delete_user_click(scope.row)">
              <el-button slot="reference" plain size="small" type="danger">删除</el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <RegisterUser ref="RegisterUserRef" @on_create="on_user_register_callback"/>
      <ChangeUserRule ref="ChangeUserRuleRef" @on_modify="on_user_modify_callback"/>
      <ResetUserPassword ref="ResetUserPasswordRef" @on_modify="on_user_modify_callback"/>
    </div>
  </div>
</template>

<script>
import RegisterUser from "./RegisterUser";
import ChangeUserRule from "./ChangeUserRule";
import ResetUserPassword from "./ResetUserPassword";

export default {
  name: "UserList",
  components: {RegisterUser, ChangeUserRule, ResetUserPassword},
  data() {
    return {
    }
  },
  created() {
    this.load_rules().then(() => this.load_users())
  },
  methods: {
    on_register_user_click() {
      this.$refs.RegisterUserRef.init_data(this.all_rules)
    },
    on_user_register_callback(user) {
      this.all_users.push(user)
    },
    reset_password_click(user) {
      this.$refs.ResetUserPasswordRef.init_data(user)
    },
    change_rule_click(user) {
      this.$refs.ChangeUserRuleRef.init_data(user, this.all_rules)
    },
    on_user_modify_callback(user) {
      let findIndex = this.all_users.findIndex(item => item.userId === user.userId)
      this.$set(this.all_users, findIndex, user)
    },
    enable_user_click(user) {
      this.post_data(`/management/user/state`, {userId: user.userId, state: 0}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_users.findIndex(item => item.userId === user.userId)
          this.$set(this.all_users, findIndex, res.data.data)
        }
      })
    },
    disable_user_click(user) {
      this.post_data(`/management/user/state`, {userId: user.userId, state: 1}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_users.findIndex(item => item.userId === user.userId)
          this.$set(this.all_users, findIndex, res.data.data)
        }
      })
    },
    delete_user_click(user) {
      this.post_data(`/management/user/destroy`, {userId: user.userId}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_users.findIndex(item => item.userId === user.userId)
          this.$delete(this.all_users, findIndex)
        }
      })
    },
    on_rule_create(rule){
      this.all_rules.push(rule)
    },
    on_rule_modify(rule){
      let findIndex = this.all_rules.findIndex(item => item.id === rule.id)
      this.$set(this.all_rules, findIndex, rule)
    },
    on_rule_destroy(rule){
      let findIndex = this.all_rules.findIndex(item => item.id === rule.id)
      this.$delete(this.all_rules, findIndex)
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