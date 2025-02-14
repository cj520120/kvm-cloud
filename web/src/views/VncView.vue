<template>
	<div class="vnc-container">
		<el-container>
			<el-header class="toolbar" height="40px">
				<div class="brand">
					<img class="brand-logo" src="../assets/logo.png" />
					<span class="brand-name">KVM Cloud</span>
				</div>

				<div class="controls">
					<div class="status">
						{{ connectionStatus }}
					</div>
				</div>
			</el-header>
			<el-container>
				<el-aside :width="sidebarWidth" class="keybord-tools">
					<div class="toggle-button" @click="toggleCollapse">
						<i :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
					</div>
					<el-container v-show="!isCollapse">
						<el-header height="30px">
							<!-- 输入类型选择 -->
							<el-select v-model="activeInputType" placeholder="请选择" size="mini" style="margin-left: 10px">
								<el-option v-for="(type, index) in inputTypes" :key="index" :label="type.label" :value="index" />
							</el-select>
						</el-header>
						<el-main>
							<!-- 字母键盘 -->
							<KeyboardLetters v-if="activeInputType === 0" @send-key="sendKey" />

							<!-- 数字键盘 -->
							<KeyboardNumbers v-if="activeInputType === 1" @send-key="sendKey" />

							<!-- 符号键盘 -->
							<KeyboardSymbols v-if="activeInputType === 2" @send-shift-key="sendShiftKey" @send-key="sendKey" />

							<!-- 功能键 -->
							<KeyboardFunctions v-if="activeInputType === 3" @send-key="sendKey" />
							<!-- 组合键控制 -->
							<KeyboardController v-if="activeInputType === 4" @send-controller-key="sendControllerKey" />
						</el-main>

						<el-footer v-if="[0, 1, 2].includes(activeInputType)">
							<!-- 公共按键 -->
							<KeyboardGlobal @send-key="sendKey" />
						</el-footer>
					</el-container>
				</el-aside>
				<el-main class="vnc-container">
					<div ref="vncContainer"></div>
				</el-main>
			</el-container>
		</el-container>
	</div>
</template>

<script>
import RFB from '@novnc/novnc/core/rfb'
import { getGuestVncPassword } from '@/api/api'
import keysymMixin from '@/api/keysym'
import KeyboardLetters from '@/components/KeyboardLetters'
import KeyboardNumbers from '@/components/KeyboardNumbers'
import KeyboardSymbols from '@/components/KeyboardSymbols'
import KeyboardFunctions from '@/components/KeyboardFunctions'
import KeyboardGlobal from '@/components/KeyboardGlobal'
import KeyboardController from '@/components/KeyboardController'

export default {
	name: 'VncView',
	components: {
		KeyboardLetters,
		KeyboardNumbers,
		KeyboardSymbols,
		KeyboardFunctions,
		KeyboardGlobal,
		KeyboardController
	},
	mixins: [keysymMixin],
	inject: ['check_full_screen'],
	data() {
		return {
			isCollapse: false,
			comboKeybord: ['Ctrl', 'Shift', 'Alt', 'Del'],
			inputTypes: [{ label: '字母' }, { label: '数字' }, { label: '符号' }, { label: '功能键' }, { label: '组合键' }],
			activeInputType: 0,
			modifierKeys: [],
			rfb: null,
			connectionStatus: '连接中...',
			guestId: this.$route.query.id,
			guestDesc: this.$route.query.description
		}
	},
	computed: {
		sidebarWidth() {
			return this.isCollapse ? '64px' : '240px'
		},
		websocketUrl() {
			const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
			return process.env.NODE_ENV === 'production' ? `${protocol}://${window.location.host}/api/vnc/${this.guestId}` : `${protocol}://192.168.2.193:8080/api/vnc/${this.guestId}`
		}
	},
	mounted() {
		this.check_full_screen(true)
		this.initVncConnection()
	},
	beforeDestroy() {
		this.check_full_screen(false)
		this.disconnectVnc()
	},
	methods: {
		toggleCollapse() {
			this.isCollapse = !this.isCollapse
		},
		async initVncConnection() {
			try {
				const res = await getGuestVncPassword({ guestId: this.guestId })
				if (res.code !== 0) throw new Error('获取密码失败')

				this.rfb = new RFB(this.$refs.vncContainer, this.websocketUrl, {
					credentials: { password: res.data },
					scaleViewport: true,
					resizeSession: true
				})

				this.rfb.addEventListener('connect', this.handleConnect)
				this.rfb.addEventListener('disconnect', this.handleDisconnect)
				this.rfb.addEventListener('credentialsrequired', this.handleAuthRequired)
			} catch (error) {
				this.connectionStatus = `连接失败: ${error.message}`
			}
		},

		handleConnect() {
			this.connectionStatus = `已连接至 ${this.guestDesc}`
		},

		handleDisconnect(event) {
			this.connectionStatus = event.detail.clean ? `正在重连 ${this.guestDesc}...` : `连接已断开 ${this.guestDesc}`
			if (event.detail.clean) this.initVncConnection()
		},

		async handleAuthRequired() {
			const res = await getGuestVncPassword({ guestId: this.guestId })
			if (res.code === 0) this.rfb.sendCredentials({ password: res.data })
		},

		sendKey(keysym, code = '', down = true) {
			if (keysym && this.rfb) {
				this.rfb.sendKey(this[keysym], code, down)
			}
		},

		sendShiftKey(keysym) {
			console.log(keysym)
			this.sendKey('XK_Shift_L', 'ShiftLeft', true)
			this.sendKey(keysym)
			this.sendKey('XK_Shift_L', 'ShiftLeft', false)
		},
		sendControllerKey(modifierKeys) {
			this.handleModifiers(modifierKeys, 'press')
			this.handleModifiers(modifierKeys, 'release')
		},
		handleModifiers(modifierKeys, action) {
			const modifiers = {
				Ctrl: { key: this.XK_Control_L, code: 'ControlLeft' },
				Shift: { key: this.XK_Shift_L, code: 'ShiftLeft' },
				Alt: { key: this.XK_Alt_L, code: 'AltLeft' },
				Del: { key: this.XK_Delete, code: 'Delete' }
			}
			modifierKeys.forEach((mod) => {
				if (modifiers[mod]) this.rfb.sendKey(modifiers[mod].key, modifiers[mod].code, action === 'press')
			})
		},

		disconnectVnc() {
			if (this.rfb) {
				this.rfb.disconnect()
				this.rfb = null
			}
		}
	}
}
</script>

<style scoped>
.vnc-container {
	background: #2c3e50;
	min-height: calc(100vh - 40px);
	/* min-width: 100vw; */
}

.toolbar {
	display: flex;
	align-items: center;
	background: #34495e;
	border-bottom: 1px solid #2c3e50;
	padding: 0 20px;
}

.brand {
	display: flex;
	align-items: center;
	margin-right: 30px;
}

.brand-logo {
	width: 16px;
	height: 16px;
	margin-right: 8px;
}

.brand-name {
	color: #ecf0f1;
	font-weight: bold;
}

.controls {
	flex: 1;
	display: flex;
	align-items: center;
	gap: 15px;
}

.control-group {
	padding: 5px 10px;
	border-radius: 4px;
	color: white;
}

.common-keys {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 4px;
}

.common-keys > :first-child {
	margin-left: 10px;
}
.status {
	margin-left: auto;
	color: #95a5a6;
	font-size: 0.9em;
}
.keybord-tools {
	background: #2c3e50;
}
.vnc-container {
	background: rgb(40, 40, 40);
}
.toggle-button {
	padding: 15px;
	text-align: center;
	color: #fff;
	font-size: 20px;
	cursor: pointer;
	background-color: #434a50;
}

.toggle-button:hover {
	background-color: #363b3f;
}
</style>