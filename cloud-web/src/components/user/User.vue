<template>
  <div class="main">
    <Top/>
    <div class="container" style="display: flex">
      <Menu :index="menuIndex"/>
      <el-main>
        <el-tabs v-model="activeName" type="card">
          <el-tab-pane label="用户管理" name="UserList">
            <UserList ref="UserListRef"/>
          </el-tab-pane>
          <el-tab-pane label="权限管理" name="RoleList">
            <RuleList @on_rule_create="on_rule_create" @on_rule_modify="on_rule_modify" @on_rule_destroy="on_rule_destroy"/>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </div>
  </div>
</template>

<script>
import UserList from './UserList'
import Top from "../common/Top";
import Menu from "../common/Menu";
import RuleList from "./RuleList";
export default {
  name: "User.vue",
  components:{Top,Menu,UserList,RuleList},
  data() {
    return {
      activeName: "UserList",
      menuIndex:"11"
    }
  },
  methods:{
    on_rule_create(rule){
      this.$refs.UserListRef.on_rule_create(rule)
    },
    on_rule_modify(rule){
      this.$refs.UserListRef.on_rule_modify(rule)
    },
    on_rule_destroy(rule){
      this.$refs.UserListRef.on_rule_destroy(rule)
    }
  }
}
</script>

<style scoped>
.main{
  height: 100%;
}
.container{
  height: calc(100%  - 60px);;
}
</style>