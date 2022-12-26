<template>
	<div id="app">
		<HeadViewVue v-if="!isFullScreen" />
		<el-container>
			<NavViewVue v-if="!isFullScreen" />
			<el-main class="main-container" :class="isFullScreen ? 'full_screen_class' : ''">
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
	},
	provide() {
		return {
			check_full_screen: (isFullScreen) => {
				this.isFullScreen = isFullScreen
			}
		}
	},
	data() {
		return {
			isFullScreen: false
		}
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
.full_screen_class {
	padding: 0 !important;
	height: 100vh !important;
	max-height: none !important;
}
</style>
