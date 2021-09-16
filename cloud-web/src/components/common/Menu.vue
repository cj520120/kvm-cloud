<template>
  <el-menu
      :default-active="index"
      :router="true"
      active-text-color="#ffd04b"
      background-color="#34495e"
      :class="isCollapse?'menu_un_collapse':'menu_collapse'"
      text-color="#fff">
    <div style="background: #25262d;text-align: center;height: 30px;color: white;line-height: 30px;cursor: pointer;font-size: 18px"  @click="change_menu_collapse">
      <i id="menu_collapse" :class="isCollapse?'el-icon-s-fold':'el-icon-s-unfold'" ></i>
    </div>
    <el-menu-item :route="bind_menu_path('Instance')" index="1">
      <i class="layui-icon layui-icon-app"></i>
      <span v-show="isCollapse" slot="title">虚拟机管理</span>
    </el-menu-item>
    <el-menu-item :route="bind_menu_path('Cluster')" index="2">
      <i class="layui-icon layui-icon-location"></i>
      <span v-show="isCollapse" slot="title">集群管理</span>
    </el-menu-item>
    <el-menu-item :route="bind_menu_path('Network')" index="3">
      <i class="layui-icon layui-icon-website"></i>
      <span v-show="isCollapse" slot="title">网络管理</span>
    </el-menu-item>
    <el-menu-item :route="bind_menu_path('Host')" index="4">
      <i class="layui-icon layui-icon-slider"></i>
      <span v-show="isCollapse" slot="title">主机管理</span>
    </el-menu-item>
    <el-menu-item :route="bind_menu_path('Template')" index="5">
      <i class="layui-icon layui-icon-template-1"></i>
      <span v-show="isCollapse" slot="title">模版管理</span>
    </el-menu-item>
    <el-menu-item :route="bind_menu_path('Storage')" index="6">
      <i class="layui-icon layui-icon-note"></i>
      <span v-show="isCollapse" slot="title">存储管理</span>
    </el-menu-item>
    <el-menu-item :route="bind_menu_path('Volume')" index="7">
      <i class="layui-icon layui-icon-senior"></i>
      <span v-show="isCollapse" slot="title">磁盘管理</span>
    </el-menu-item>
    <el-menu-item :route="bind_menu_path('Scheme')" index="8">
      <i class="layui-icon layui-icon-theme"></i>
      <span v-show="isCollapse" slot="title">计算方案</span>
    </el-menu-item>
    <el-menu-item :route="bind_menu_path('Category')" index="9">
      <i class="layui-icon layui-icon-windows"></i>
      <span v-show="isCollapse" slot="title">系统分类</span>
    </el-menu-item>
    <el-menu-item :route="bind_menu_path('Group')" index="10">
      <i class="layui-icon layui-icon-table"></i>
      <span v-show="isCollapse" slot="title">群组管理</span>
    </el-menu-item>
    <el-menu-item :route="bind_menu_path('User')" index="11">
      <i class="layui-icon layui-icon-username"></i>
      <span v-show="isCollapse" slot="title">用户管理</span>
    </el-menu-item>
  </el-menu>
</template>

<script>

export default {
  name: "Menu.vue",
  props: {
    index: String
  },
  data() {
    return {
      isCollapse: true
    }
  },
  created() {
    let collapse=localStorage.getItem("menu_is_collapse")
    this.isCollapse = collapse==="0"?false:true
  },
  methods: {
    change_menu_collapse() {
      this.isCollapse = !this.isCollapse
      localStorage.setItem("menu_is_collapse", this.isCollapse?"1":"0")
    },
    bind_menu_path(path) {
      return {path: path}
    }
  }
}
</script>

<style scoped>
@import "../../assets/css/iconfont.css";

.menu_collapse {
  width: 60px;
  left: 0;
  bottom: 0;
  background-color: #34495e;
}
.menu_un_collapse{
  width: 200px;
  left: 0;
  bottom: 0;
  background-color: #34495e;
}

.layui-icon {
  font-size: 18px;
  color: white;
  margin-right: 5px;
}
</style>