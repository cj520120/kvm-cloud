<template>
	<el-menu :default-active="this.$route.path" router mode="horizontal">
		<el-menu-item v-for="(item, i) in nav_menu_data" :key="i" :index="item.path">
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
				{ path: '/Guest', title: '虚拟机管理' },
				{ path: '/Volume', title: '磁盘管理' },
				{ path: '/Snapshot', title: '快照管理' },
				{ path: '/Network', title: '网络管理' },
				{ path: '/Host', title: '主机管理' },
				{ path: '/Storage', title: '存储池管理' },
				{ path: '/Template', title: '模版管理' },
				{ path: '/Scheme', title: '计算方案' }
			]
		}
	}
}
</script>