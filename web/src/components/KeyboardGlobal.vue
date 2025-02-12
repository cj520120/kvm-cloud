<template>
	<div>
		<div class="global-group">
			<el-button size="mini" v-for="(sym, index) in symbols" :key="index" @click="$emit('send-key', sym.keysym)">
				{{ sym.display }}
			</el-button>
		</div>
	</div>
</template>
  
  <script>
export default {
	name: 'KeyboardGlobal',
	data() {
		return {
			symbols: [
				{ display: 'Space', keysym: 'XK_space', shift: false },
				{ display: 'Enter', keysym: 'XK_KP_Enter', shift: false },
				{ display: 'BackSpace', keysym: 'XK_BackSpace', shift: false }
			]
		}
	}
}
</script>
  
  <style scoped>
.global-group {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 4px;
}
.global-group > :first-child {
	margin-left: 10px;
}
</style>