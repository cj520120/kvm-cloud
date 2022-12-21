<template>
	<el-header>
		<div style="float: left; background-color: #34495e; height: 100%">
			<h5 style="font-size: 18px; margin: 20px; color: white">Roamblue KVM Cloud</h5>
		</div>
		<el-menu class="headerMenu" mode="horizontal" @select="handleSelect" background-color="#34495e" text-color="#fff" active-text-color="#ffd04b">
			<el-menu-item index="dingdan1" id="dingdan1">退出</el-menu-item>
		</el-menu>
	</el-header>
</template>
  
<script>
export default {
	data() {
		return {}
	},
	methods: {
		handleSelect() {}
	}
}
</script> 
<style scoped>
.el-header {
	padding: 0;
	width: 100%;
}
.headerMenu {
	display: flex;
	flex-direction: row-reverse;
}
</style>