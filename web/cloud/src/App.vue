<template>
	<div id="app">
		<HeadViewVue />
		<el-container>
			<NavViewVue />
			<el-main class="main-container">
				<router-view></router-view>
			</el-main>
		</el-container>
	</div>
</template>
<script>
import NavViewVue from './views/NavView.vue'
import HeadViewVue from './views/HeadView.vue'
export default {
	components: {
		NavViewVue,
		HeadViewVue
	}
}
</script>

<style>
body,
html,
#app,
#app > div {
	width: 100vw;
	height: 100vh;
	margin: 0;
	overflow: hidden !important;
}
.el-aside {
	height: calc(100vh - 60px) !important;
}
.el-main {
	max-height: calc(100vh - 60px) !important;
	overflow: hidden auto !important;
}
.main-container {
	overflow: hidden !important;
	max-height: calc(100vh - 60px) !important;
}
</style>
