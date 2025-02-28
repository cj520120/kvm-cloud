<template>
	<div class="keyboard-letters">
		<el-checkbox size="mini" v-model="uppercase" class="uppercase-toggle">大写</el-checkbox>
		<div class="letter-buttons">
			<el-button size="mini" v-for="(letters, index) in currentLetters" :key="'shift-' + index" @click="$emit('send-key', letters.keysym)">
				{{ letters.display }}
			</el-button>
		</div>
	</div>
</template>
  
  <script>
export default {
	name: 'KeyboardLetters',
	data() {
		return {
			uppercase: false
		}
	},
	computed: {
		currentLetters() {
			const baseChars = Array.from({ length: 26 }, (_, i) => {
				let code = 97 + i // ASCII a-z
				let display = this.uppercase ? String.fromCharCode(code - 32) : String.fromCharCode(code)
				return {
					display: display,
					keysym: `XK_${display}`
				}
			})
			return baseChars
		}
	}
}
</script>
  
  <style scoped>
.letter-buttons {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 4px;
}
.letter-buttons > :first-child {
	margin-left: 10px;
}
.uppercase-toggle {
	margin-bottom: 8px;
	margin-left: 10px;
	color: white;
}
</style>