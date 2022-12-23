<template>
	<el-menu class="el-menu-vertical-demo" :default-active="this.$route.path" router background-color="rgb(52, 73, 94)" text-color="#fff" active-text-color="#ffd04b">
		<el-menu-item v-for="(item, i) in nav_menu_data" :key="i" :index="item.path">
			<i :class="item.icon"></i>
			{{ item.title }}
		</el-menu-item>
	</el-menu>
</template>
  
<script>
export default {
	data() {
		return {
			path: '',
			nav_menu_data: [
				{ path: '/', title: '主页', icon: 'el-icon-house' },
				{ path: '/Guest', title: '虚拟机管理', icon: 'el-icon-s-platform' },
				{ path: '/Volume', title: '磁盘管理', icon: 'el-icon-bank-card' },
				{ path: '/Snapshot', title: '快照管理', icon: 'el-icon-copy-document' },
				{ path: '/Network', title: '网络管理', icon: 'el-icon-platform-eleme' },
				{ path: '/Host', title: '主机管理', icon: 'el-icon-monitor' },
				{ path: '/Storage', title: '存储池管理', icon: 'el-icon-coin' },
				{ path: '/Template', title: '模版管理', icon: 'el-icon-price-tag' },
				{ path: '/Scheme', title: '计算方案', icon: 'el-icon-cpu' }
			]
		}
	}
}
</script> 
<style scoped>
.el-menu-vertical-demo {
	height: calc(100vh - 60px);
}
</style>