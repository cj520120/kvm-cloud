<template>
	<div class="main">
		<el-container>
			<el-header class="tool_tip" height="40px" align="middle" justify="center">
				<div class="logo">
					<div style="text-align: center; height: 40px; align: center; float: left">
						<img src="../assets/logo.png" style="margin-top: 10px; width: 16px; height: 16px" />
					</div>
					<div style="float: left; margin-left: 5px; font: bold 14px Helvetica; color: grary; padding-top: 11px; width: 100px">KVM Cloud</div>
				</div>
				<div class="controller_div">
					<div class="send_ctl">
						<el-select v-model="show_input_type" placeholder="请选择" size="mini" style="width: 100px">
							<el-option label="字母" :value="0"></el-option>
							<el-option label="数字" :value="1"></el-option>
							<el-option label="符号" :value="2"></el-option>
							<el-option label="功能键" :value="3"></el-option>
							<el-option label="组合键" :value="4"></el-option>
						</el-select>
					</div>
					<div class="send_ctl" v-if="show_input_type === 4">
						<el-checkbox style="color: white" v-model="combox.check_ctrl">Ctrl</el-checkbox>
						<el-checkbox style="color: white" v-model="combox.check_shift">Shift</el-checkbox>
						<el-checkbox style="color: white" v-model="combox.check_alt">Alt</el-checkbox>
						<el-checkbox style="color: white" v-model="combox.check_del">Del</el-checkbox>
					</div>
					<div class="send_ctl" v-if="show_input_type === 4">
						<button @click="sendCombox()" size="mini" style="margin-left: 10px">只发送功能</button>
					</div>
					<div class="send_ctl" v-if="show_input_type === 1">
						<button @click="sendKey(XK_1)">1</button>
						<button @click="sendKey(XK_2)">2</button>
						<button @click="sendKey(XK_3)">3</button>
						<button @click="sendKey(XK_4)">4</button>
						<button @click="sendKey(XK_5)">5</button>
						<button @click="sendKey(XK_6)">6</button>
						<button @click="sendKey(XK_7)">7</button>
						<button @click="sendKey(XK_8)">8</button>
						<button @click="sendKey(XK_9)">9</button>
						<button @click="sendKey(XK_0)">0</button>
					</div>

					<div class="send_ctl" v-if="show_input_type === 0">
						<el-checkbox style="color: white" v-model="combox.check_uppercase">大写</el-checkbox>
					</div>
					<div class="send_ctl" v-if="show_input_type === 0 && !combox.check_uppercase">
						<button @click="sendKey(XK_a)">a</button>
						<button @click="sendKey(XK_b)">b</button>
						<button @click="sendKey(XK_c)">c</button>
						<button @click="sendKey(XK_d)">d</button>
						<button @click="sendKey(XK_e)">e</button>
						<button @click="sendKey(XK_f)">f</button>
						<button @click="sendKey(XK_g)">g</button>
						<button @click="sendKey(XK_h)">h</button>
						<button @click="sendKey(XK_i)">i</button>
						<button @click="sendKey(XK_j)">j</button>
						<button @click="sendKey(XK_k)">k</button>
						<button @click="sendKey(XK_l)">l</button>
						<button @click="sendKey(XK_m)">m</button>
						<button @click="sendKey(XK_n)">n</button>
						<button @click="sendKey(XK_o)">o</button>
						<button @click="sendKey(XK_p)">p</button>
						<button @click="sendKey(XK_q)">q</button>
						<button @click="sendKey(XK_r)">r</button>
						<button @click="sendKey(XK_s)">s</button>
						<button @click="sendKey(XK_t)">t</button>
						<button @click="sendKey(XK_u)">u</button>
						<button @click="sendKey(XK_v)">v</button>
						<button @click="sendKey(XK_w)">w</button>
						<button @click="sendKey(XK_x)">x</button>
						<button @click="sendKey(XK_y)">y</button>
						<button @click="sendKey(XK_z)">z</button>
					</div>

					<div class="send_ctl" v-if="show_input_type === 0 && combox.check_uppercase">
						<button @click="sendKey(XK_A)">A</button>
						<button @click="sendKey(XK_B)">B</button>
						<button @click="sendKey(XK_C)">C</button>
						<button @click="sendKey(XK_D)">D</button>
						<button @click="sendKey(XK_E)">E</button>
						<button @click="sendKey(XK_F)">F</button>
						<button @click="sendKey(XK_G)">G</button>
						<button @click="sendKey(XK_H)">H</button>
						<button @click="sendKey(XK_I)">I</button>
						<button @click="sendKey(XK_J)">J</button>
						<button @click="sendKey(XK_K)">K</button>
						<button @click="sendKey(XK_L)">L</button>
						<button @click="sendKey(XK_M)">M</button>
						<button @click="sendKey(XK_N)">N</button>
						<button @click="sendKey(XK_O)">O</button>
						<button @click="sendKey(XK_P)">P</button>
						<button @click="sendKey(XK_Q)">Q</button>
						<button @click="sendKey(XK_R)">R</button>
						<button @click="sendKey(XK_S)">S</button>
						<button @click="sendKey(XK_T)">T</button>
						<button @click="sendKey(XK_U)">U</button>
						<button @click="sendKey(XK_V)">V</button>
						<button @click="sendKey(XK_W)">W</button>
						<button @click="sendKey(XK_X)">X</button>
						<button @click="sendKey(XK_Y)">Y</button>
						<button @click="sendKey(XK_Z)">Z</button>
					</div>
					<div key="send_ctl" v-if="show_input_type === 2">
						<button @click="sendKey(XK_quoteleft)">`</button>
						<button @click="send_shift_key(XK_quoteleft)">~</button>
						<button @click="send_shift_key(XK_1)">!</button>
						<button @click="send_shift_key(XK_2)">@</button>
						<button @click="send_shift_key(XK_3)">#</button>
						<button @click="send_shift_key(XK_4)">$</button>
						<button @click="send_shift_key(XK_5)">%</button>
						<button @click="send_shift_key(XK_6)">^</button>
						<button @click="send_shift_key(XK_7)">&amp;</button>
						<button @click="send_shift_key(XK_8)">*</button>
						<button @click="send_shift_key(XK_9)">(</button>
						<button @click="send_shift_key(XK_0)">)</button>
						<button @click="send_shift_key(XK_equal)">+</button>
						<button @click="sendKey(XK_minus)">-</button>
						<button @click="send_shift_key(XK_minus)">_</button>
						<button @click="sendKey(XK_equal)">=</button>
						<button @click="sendKey(XK_slash)">/</button>
						<button @click="sendKey(XK_bar)">\</button>
						<button @click="sendKey(XK_braceleft)">[</button>
						<button @click="sendKey(XK_braceright)">]</button>
						<button @click="send_shift_key(XK_braceleft)">{</button>
						<button @click="send_shift_key(XK_braceright)">}</button>
						<button @click="send_shift_key(XK_bar)">|</button>
						<button @click="send_shift_key(XK_colon)">:</button>
						<button @click="sendKey(XK_colon)">;</button>
						<button @click="sendKey(XK_quotedbl)">'</button>
						<button @click="send_shift_key(XK_quotedbl)">"</button>
						<button @click="sendKey(XK_less)">,</button>
						<button @click="sendKey(XK_period)">.</button>
						<button @click="send_shift_key(XK_less)">&lt;</button>
						<button @click="send_shift_key(XK_period)">&gt;</button>
					</div>
					<div class="send_ctrl" v-if="show_input_type === 0 || show_input_type === 1 || show_input_type === 2">
						<button @click="sendKey(XK_space)">Space</button>
						<button @click="sendKey(XK_KP_Enter)">Enter</button>
						<button @click="sendKey(XK_BackSpace)">Backspace</button>
					</div>
					<div class="send_ctl" v-if="show_input_type === 3">
						<button @click="sendKey(XK_Escape)">ESC</button>
						<button @click="sendKey(XK_F1)">F1</button>
						<button @click="sendKey(XK_F2)">F2</button>
						<button @click="sendKey(XK_F3)">F3</button>
						<button @click="sendKey(XK_F4)">F4</button>
						<button @click="sendKey(XK_F5)">F5</button>
						<button @click="sendKey(XK_F6)">F6</button>
						<button @click="sendKey(XK_F7)">F7</button>
						<button @click="sendKey(XK_F8)">F8</button>
						<button @click="sendKey(XK_F9)">F9</button>
						<button @click="sendKey(XK_F10)">F10</button>
						<button @click="sendKey(XK_F11)">F11</button>
						<button @click="sendKey(XK_F12)">F12</button>
						<button @click="sendKey(XK_Tab)">Tab</button>
						<button @click="sendKey(XK_Left)">Left</button>
						<button @click="sendKey(XK_Right)">Right</button>
						<button @click="sendKey(XK_Up)">Up</button>
						<button @click="sendKey(XK_Down)">Down</button>

						<button @click="sendKey(XK_Page_Up)">PageUp</button>
						<button @click="sendKey(XK_Page_Down)">PageDown</button>
					</div>
					<div class="status_tool_tip">
						{{ status }}
					</div>
				</div>
			</el-header>

			<el-main>
				<div ref="vncContainer" style="margin-top: 30px"></div>
			</el-main>

			<el-footer class="footer" height="40px">
				<div></div>
			</el-footer>
		</el-container>
	</div>
</template>
    
<script>
import RFB from '@novnc/novnc/core/rfb'

import { getGuestVncPassword } from '@/api/api'
import keysym from '@/api/keysym'
export default {
	name: 'VncView',
	inject: ['check_full_screen'],
	data() {
		return {
			id: 0,
			description: '',
			rfb: null,
			url: '',
			status: '连接中...',
			combox: {
				check_ctrl: false,
				check_alt: false,
				check_shift: false,
				check_del: false,
				check_uppercase: false
			},
			show_input_type: 2
		}
	},
	mixins: [keysym],
	mounted() {
		this.check_full_screen(true)
		this.id = this.$route.query.id
		this.description = this.$route.query.description
		let protocol
		if (window.location.protocol === 'https:') {
			protocol = 'wss'
		} else {
			protocol = 'ws'
		}
		this.status = `正在连接[${this.description}]...`
		this.url = process.env.NODE_ENV === 'production' ? `${protocol}://${window.location.host}/api/vnc/${this.id}` : `${protocol}://192.168.2.193:8080/api/vnc/${this.id}`
		this.connect()
	},
	beforeDestroy() {
		this.check_full_screen(false)
	},
	methods: {
		connect() {
			getGuestVncPassword({ guestId: this.id }).then((res) => {
				if (res.code === 0) {
					const container = this.$refs.vncContainer
					this.rfb = new RFB(container, this.url, { credentials: { password: res.data } })
					this.rfb.addEventListener('connect', this.on_connect_success)
					this.rfb.addEventListener('disconnect', this.on_disconnect)
					this.rfb.addEventListener('credentialsrequired', this.on_required_password)
					this.rfb.scaleViewport = false
					this.rfb.resizeSession = true
				}
			})
		},
		on_connect_success() {
			this.status = `连接成功[${this.description}]`
		},
		on_disconnect(msg) {
			this.status = `远程连接已断开[${this.description}]`
			if (msg.detail.clean) {
				this.status = `正在重新连接[${this.description}]...`
				this.connect()
			}
		},
		on_required_password() {
			getGuestVncPassword({ guestId: this.id }).then((res) => {
				if (res.code === 0 && res.data) {
					this.rfb.sendCredentials({ password: res.data })
				}
			})
		},
		on_send_ctrl_alt_del() {
			this.rfb.sendCtrlAltDel()
			this.rfb.focus()
		},
		sendKey(keysym, code, down) {
			if (this.combox.check_ctrl) {
				this.rfb.sendKey(this.XK_Control_L, 'ControlLeft', true)
			}
			if (this.combox.check_alt) {
				this.rfb.sendKey(this.XK_Alt_L, 'AltLeft', true)
			}

			if (this.combox.check_del) {
				this.rfb.sendKey(this.XK_Delete, 'Delete', true)
			}

			if (this.combox.check_shift) {
				this.rfb.sendKey(this.XK_Shift_L, 'ShiftLeft', true)
			}
			this.rfb.sendKey(keysym, code, down)
			if (this.combox.check_ctrl) {
				this.rfb.sendKey(this.XK_Control_L, 'ControlLeft', false)
			}
			if (this.combox.check_alt) {
				this.rfb.sendKey(this.XK_Alt_L, 'AltLeft', false)
			}

			if (this.combox.check_del) {
				this.rfb.sendKey(this.XK_Delete, 'Delete', false)
			}

			if (this.combox.check_shift) {
				this.rfb.sendKey(this.XK_Shift_L, 'ShiftLeft', false)
			}
			this.combox.check_ctrl = false
			this.combox.check_alt = false
			this.combox.check_shift = false
			this.combox.check_del = false
		},
		sendCombox() {
			if (this.combox.check_ctrl) {
				this.rfb.sendKey(this.XK_Control_L, 'ControlLeft', true)
			}
			if (this.combox.check_alt) {
				this.rfb.sendKey(this.XK_Alt_L, 'AltLeft', true)
			}

			if (this.combox.check_del) {
				this.rfb.sendKey(this.XK_Delete, 'Delete', true)
			}

			if (this.combox.check_shift) {
				this.rfb.sendKey(this.XK_Shift_L, 'ShiftLeft', true)
			}

			if (this.combox.check_ctrl) {
				this.rfb.sendKey(this.XK_Control_L, 'ControlLeft', false)
			}
			if (this.combox.check_alt) {
				this.rfb.sendKey(this.XK_Alt_L, 'AltLeft', false)
			}

			if (this.combox.check_del) {
				this.rfb.sendKey(this.XK_Delete, 'Delete', false)
			}

			if (this.combox.check_shift) {
				this.rfb.sendKey(this.XK_Shift_L, 'ShiftLeft', false)
			}
			this.combox.check_ctrl = false
			this.combox.check_alt = false
			this.combox.check_shift = false
			this.combox.check_del = false
		},
		send_shift_key(keysym) {
			this.rfb.sendKey(this.XK_Shift_L, 'ShiftLeft', true)
			this.rfb.sendKey(keysym)
			this.rfb.sendKey(this.XK_Shift_L, 'ShiftLeft', false)
		}
	}
}
</script>
    
    <style scoped>
.main {
	margin: 0px;
	padding: 0px;
	background-color: rgb(40, 40, 40);
	min-height: 100vh;
	min-width: 100vw;
	height: fit-content;
	width: fit-content;
	display: flex;
	flex-direction: column;
}
.tool_tip {
	background-color: rgb(92, 92, 92);
	color: white;
	font: bold 12px Helvetica;
	border-bottom: 1px outset;
	padding: 0px;
	border-bottom: 1px outset;
	display: flex;
	align-items: center;
	left: 0;
	top: 0;
	position: fixed;
	width: 100%;
}
.tool_tip .logo {
	padding: 0px;
	padding-left: 20px;
	margin-top: 0px;
	width: 200px;
	height: 100%;
	float: left;
}
.tool_tip .status {
	width: calc(100% - 200px) !important;
	height: 100%;
	float: left;
	line-height: 40px;
}
footer {
	color: white;
	font: bold 12px Helvetica;
	border-bottom: 1px outset;
}

#screen {
	flex: 1; /* fill remaining space */
	overflow: hidden;
	width: 100vw;
	height: 100vh;
}
.controller_div {
	color: white;
	float: left;
	display: flex;
	align-items: center;
}
.controller_div .send_ctl {
	float: left;
	display: flex;
	align-items: center;
	padding-right: 10px;
}
.button--mini,
.button--mini.is-round {
	padding: 5px;
}
.controller_div .status_tool_tip {
	float: right;
	display: flex;
	align-items: center;
	padding-right: 10px;
}
</style>