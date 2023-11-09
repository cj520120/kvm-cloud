<template>
	<div id="LayoutLeft" class="layout-left">
		<div @click="shrinkMenu" class="shrink">
			<i :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
		</div>
		<el-menu :collapse="isCollapse" class="el-menu-vertical-demo" :default-active="this.$route.path" router :collapse-transition="false" background-color="rgb(52, 73, 94)" text-color="#fff" active-text-color="#ffd04b">
			<el-menu-item v-for="(item, i) in nav_menu_data" :key="i" :index="item.path">
				<i :class="item.icon"></i>
				<span slot="title">{{ item.title }}</span>
			</el-menu-item>
		</el-menu>
	</div>
</template>
// 
<script>
export default {
	data() {
		return {
			path: '',
			isCollapse: true,
			nav_menu_data: [
				{ path: '/', title: '主页', icon: 'el-icon-house' },
				{ path: '/Guest', title: '虚拟机管理', icon: 'el-icon-s-platform' },
				{ path: '/Host', title: '主机管理', icon: 'el-icon-monitor' },
				{ path: '/Network', title: '网络管理', icon: 'el-icon-platform-eleme' },
				{ path: '/Storage', title: '存储池管理', icon: 'el-icon-coin' },
				{ path: '/Template', title: '模版管理', icon: 'el-icon-price-tag' },
				{ path: '/Scheme', title: '计算方案', icon: 'el-icon-cpu' },
				{ path: '/Volume', title: '磁盘管理', icon: 'el-icon-bank-card' },
				{ path: '/Snapshot', title: '快照管理', icon: 'el-icon-copy-document' },
				{ path: '/Group', title: '群组管理', icon: 'el-icon-folder' },
				{ path: '/User', title: '用户管理', icon: 'el-icon-s-custom' }
			]
		}
	},
	created() {
		this.isCollapse = localStorage.getItem('menu_is_collapse') === '0' ? false : true
	},
	methods: {
		shrinkMenu() {
			this.isCollapse = !this.isCollapse
			localStorage.setItem('menu_is_collapse', this.isCollapse ? '1' : '0')
		}
	}
}
</script> 
<style scoped>
.el-menu-vertical-demo {
	height: calc(100vh - 60px);
	overflow: hidden auto;
}
.layout-left {
	position: relative;
}
.shrink {
	height: 30px;
	background: rgb(37, 38, 45);
	color: white;
	width: calc(100% - 1px);
	display: flex;
	justify-content: center;
	align-items: center;
	cursor: pointer;
	z-index: 9999;
}
</style>