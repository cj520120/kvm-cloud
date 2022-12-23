<template>
	<div id="app">
		<router-view />
	</div>
</template>

<style>
body , html, #app, #app> div{
	width: 100vw;
	height: 100vh;
	margin: 0;
}
.el-aside {
	height: calc(100vh - 60px) !important;
}
</style>
