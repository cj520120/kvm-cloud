<template>
	<div class="keyboard-functions">
		<div class="function-group">
			<el-button size="mini" v-for="(func, key) in functions" :key="key" @click="$emit('send-key', func.keysym)">
				{{ func.label }}
			</el-button>
		</div>
	</div>
</template>
  
  <script>
export default {
	name: 'KeyboardFunctions',
	data() {
		return {
			functions: {
				esc: { label: 'ESC', keysym: 'XK_Escape' },
				f1: { label: 'F1', keysym: 'XK_F1' },
				f2: { label: 'F2', keysym: 'XK_F2' },
				f3: { label: 'F3', keysym: 'XK_F3' },
				f4: { label: 'F4', keysym: 'XK_F4' },
				f5: { label: 'F5', keysym: 'XK_F5' },
				f6: { label: 'F6', keysym: 'XK_F6' },
				f7: { label: 'F7', keysym: 'XK_F7' },
				f8: { label: 'F8', keysym: 'XK_F8' },
				f9: { label: 'F9', keysym: 'XK_F9' },
				f10: { label: 'F10', keysym: 'XK_F10' },
				f11: { label: 'F11', keysym: 'XK_F11' },
				f12: { label: 'F12', keysym: 'XK_F12' },
				tab: { label: 'Tab', keysym: 'XK_Tab' },
				left: { label: '←', keysym: 'XK_Left' },
				right: { label: '→', keysym: 'XK_Right' },
				up: { label: '↑', keysym: 'XK_Up' },
				down: { label: '↓', keysym: 'XK_Down' },
				pgup: { label: 'PgUp', keysym: 'XK_Page_Up' },
				pgdn: { label: 'PgDn', keysym: 'XK_Page_Down' }
			}
		}
	}
}
</script>
  
  <style scoped>
.function-group {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 4px;
}
.function-group > :first-child {
	margin-left: 10px;
}
</style>