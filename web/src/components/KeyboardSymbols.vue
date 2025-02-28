<template>
	<div>
		<div class="symbol-group">
			<el-button size="mini" v-for="(sym, index) in symbols" :key="index" @click="$emit(sym.shift ? 'send-shift-key' : 'send-key', sym.keysym)">
				{{ sym.display }}
			</el-button>
		</div>
	</div>
</template>
  
  <script>
export default {
	name: 'KeyboardSymbols',
	data() {
		return {
			symbols: [
				{ display: '`', keysym: 'XK_quoteleft', shift: false },
				{ display: '-', keysym: 'XK_minus', shift: false },
				{ display: '=', keysym: 'XK_equal', shift: false },
				{ display: '/', keysym: 'XK_slash', shift: false },
				{ display: '\\', keysym: 'XK_bar', shift: false },
				{ display: '[', keysym: 'XK_braceleft', shift: false },
				{ display: ']', keysym: 'XK_braceright', shift: false },
				{ display: ';', keysym: 'XK_colon', shift: false },
				{ display: "'", keysym: 'XK_quotedbl', shift: false },
				{ display: ',', keysym: 'XK_less', shift: false },
				{ display: '.', keysym: 'XK_period', shift: false },
				{ display: '~', keysym: 'XK_quoteleft', shift: true },
				{ display: '!', keysym: 'XK_1', shift: true },
				{ display: '@', keysym: 'XK_2', shift: true },
				{ display: '#', keysym: 'XK_3', shift: true },
				{ display: '$', keysym: 'XK_4', shift: true },
				{ display: '%', keysym: 'XK_5', shift: true },
				{ display: '^', keysym: 'XK_6', shift: true },
				{ display: '&', keysym: 'XK_7', shift: true },
				{ display: '*', keysym: 'XK_8', shift: true },
				{ display: '(', keysym: 'XK_9', shift: true },
				{ display: ')', keysym: 'XK_0', shift: true },
				{ display: '_', keysym: 'XK_minus', shift: true },
				{ display: '+', keysym: 'XK_equal', shift: true },
				{ display: '{', keysym: 'XK_braceleft', shift: true },
				{ display: '}', keysym: 'XK_braceright', shift: true },
				{ display: '|', keysym: 'XK_bar', shift: true },
				{ display: ':', keysym: 'XK_colon', shift: true },
				{ display: '"', keysym: 'XK_quotedbl', shift: true },
				{ display: '<', keysym: 'XK_less', shift: true },
				{ display: '>', keysym: 'XK_period', shift: true }
			]
		}
	}
}
</script>
  
  <style scoped>
.symbol-group {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 4px;
}
.symbol-group > :first-child {
	margin-left: 10px;
}
</style>