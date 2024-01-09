<template>
	<div id="app">
		<el-header height="60px" style="background-color: #34495e; padding: 0; color: white" v-if="!isFullScreen">
			<el-container style="height: 60px">
				<div @click="go_route('/')" style="cursor: pointer; width: 200px; height: 100%">
					<img src="./assets/logo.png" style="width: 32px; height: 32px; margin-left: 20px; margin-top: 18px; float: left" />
					<div style="font-size: 18px; line-height: 50px; font-weight: bold; margin-left: 60px; margin-top: 8px">KVM Cloud</div>
				</div>
				<div style="padding: 0; float: right; margin-top: 0px; width: 100%; margin-right: 10px">
					<el-link :underline="false" class="right_menu" @click="menu_select('quit')">退出</el-link>
					<el-link :underline="false" class="right_menu" @click="menu_select('password')">修改密码</el-link>
					<el-link :underline="false" class="right_menu" @click="menu_select('support')">联系&amp;支持</el-link>
				</div>
			</el-container>
		</el-header>

		<el-container class="main-container">
			<el-aside v-if="!isFullScreen" :class="isCollapse ? 'main-aside-collapsed' : 'main-aside'" style="margin: 0; padding: 0">
				<el-button size="small" :icon="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'" @click="shrinkMenu" v-if="!isFullScreen" style="width: 100%; color: white; background-color: #334157; border: none; font-size: 16px"></el-button>
				<el-menu :collapse="isCollapse" :default-active="this.$route.path" router :collapse-transition="false" background-color="#334157" text-color="#fff" active-text-color="#ffd04b" style="border: none">
					<el-menu-item v-for="(item, i) in nav_menu_data" :key="i" :index="item.path">
						<i :class="item.icon"></i>
						<span slot="title">{{ item.title }}</span>
					</el-menu-item>
				</el-menu>
			</el-aside>

			<el-main :class="isFullScreen ? 'full_screen_class' : ''" style="margin: 0; padding: 0">
				<router-view></router-view>
			</el-main>
		</el-container>

		<el-dialog title="修改密码" :visible.sync="update_password_dialog_visible" center width="400px" :close-on-click-modal="false" :close-on-press-escape="false">
			<el-form :model="update_password" label-position="right" label-width="80px">
				<el-form-item label="旧密码" prop="oldPassword">
					<el-input v-model="update_password.oldPassword" type="password" :show-password="true"></el-input>
				</el-form-item>
				<el-form-item label="新密码" prop="newPassword">
					<el-input v-model="update_password.newPassword" type="password" :show-password="true"></el-input>
				</el-form-item>
				<el-form-item label="确认密码" prop="confirmPassword">
					<el-input v-model="update_password.confirmPassword" type="password" :show-password="true"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="update_user_password_click">确 定</el-button>
					<el-button @click="update_password_dialog_visible = false">取 消</el-button>
				</el-form-item>
			</el-form>
		</el-dialog>
		<el-dialog title="联系&amp;支持" :visible.sync="support_dialog_visible" center width="450px">
			<el-tabs v-model="supportActiveName">
				<el-tab-pane label="联系作者" name="concat">
					<div style="font-size: 15px; line-height: 30px">
						<div>&nbsp;&nbsp;&nbsp;&nbsp;本产品开源免费，遵从Apache2.0协议，个人或企业内部可自由的接入和使用。</div>
						<br />
						<div>&nbsp;&nbsp;&nbsp;&nbsp;为了保证性能，请使用SSD创建存储。数据无价，做删除操作前务必三思，建议对核心数据采用异备方案。</div>
						<br />
						<div>&nbsp;&nbsp;&nbsp;&nbsp;如果您在使用中遇到问题或相关建议，请提交issue。如遇紧急问题也可添加作者QQ(153391689)寻求帮助。</div>
						<br />
						<div>&nbsp;&nbsp;&nbsp;&nbsp;开源不易，如果KVM Cloud对您带来了帮助，请给作者买杯咖啡吧 :)</div>
						<br />
					</div>
				</el-tab-pane>
				<el-tab-pane label="微信捐赠" name="wx">
					<div class="wx" />
				</el-tab-pane>
				<el-tab-pane label="支付宝捐赠" name="zfb">
					<div class="zfb" />
				</el-tab-pane>
			</el-tabs>
		</el-dialog>
	</div>
</template>
<script>
import { getCurrentLoginSignature, modifyUserPassword } from '@/api/api'
export default {
	provide() {
		return {
			check_full_screen: (isFullScreen) => {
				this.isFullScreen = isFullScreen
			}
		}
	},
	data() {
		return {
			isFullScreen: false,

			update_password_dialog_visible: false,
			support_dialog_visible: false,
			supportActiveName: 'concat',
			update_password: {
				oldPassword: '',
				newPassword: '',
				confirmPassword: '',
				nonce: ''
			},

			path: '',
			isCollapse: true,
			nav_menu_data: [
				{ path: '/', title: '主页', icon: 'el-icon-house' },
				{ path: '/Guest', title: '虚拟机管理', icon: 'el-icon-s-platform' },
				{ path: '/Host', title: '主机管理', icon: 'el-icon-monitor' },
				{ path: '/Network', title: '网络管理', icon: 'el-icon-platform-eleme' },
				{ path: '/Storage', title: '存储池管理', icon: 'el-icon-coin' },
				{ path: '/Template', title: '模版管理', icon: 'el-icon-price-tag' },
				{ path: '/Scheme', title: '计算方案', icon: 'el-icon-cpu' },
				{ path: '/Volume', title: '磁盘管理', icon: 'el-icon-bank-card' },
				{ path: '/Snapshot', title: '快照管理', icon: 'el-icon-copy-document' },
				{ path: '/Group', title: '群组管理', icon: 'el-icon-folder' },
				{ path: '/User', title: '用户管理', icon: 'el-icon-s-custom' }
			]
		}
	},
	created() {
		this.isCollapse = localStorage.getItem('menu_is_collapse') === '0' ? false : true
	},
	methods: {
		go_route(path) {
			this.$router.push({ path: path })
		},
		shrinkMenu() {
			this.isCollapse = !this.isCollapse
			localStorage.setItem('menu_is_collapse', this.isCollapse ? '1' : '0')
		},
		menu_select(data) {
			switch (data) {
				case 'support':
					this.support_dialog_visible = true
					break
				case 'password':
					this.update_password.oldPassword = ''
					this.update_password.newPassword = ''
					this.update_password.confirmPassword = ''
					this.update_password.nonce = ''
					this.update_password_dialog_visible = true
					break
				case 'quit':
					this.$confirm(`退出登录, 是否继续?`, '提示', {
						confirmButtonText: '确定',
						cancelButtonText: '取消',
						type: 'warning'
					})
						.then(() => {
							localStorage.removeItem('X-Token')
							this.$router.push({ path: '/Login' })
						})
						.catch(() => {})
					break
			}
		},
		update_user_password_click() {
			if (this.update_password.newPassword != this.update_password.confirmPassword) {
				this.$notify.error({
					title: '错误',
					duration: 3000,
					message: `新密码与旧密码不一致`
				})
				return
			}
			getCurrentLoginSignature().then((res) => {
				if (res.code === 0) {
					let oldPassword = window.sha256_digest(window.sha256_digest(this.update_password.oldPassword + ':' + res.data.signature) + ':' + res.data.nonce)
					let nonce = res.data.nonce
					let newPassword = window.sha256_digest(this.update_password.newPassword + ':' + res.data.signature)
					let update_request = {
						oldPassword: oldPassword,
						newPassword: newPassword,
						nonce: nonce
					}
					modifyUserPassword(update_request).then((res) => {
						if (res.code !== 0) {
							this.$notify.error({
								title: '错误',
								message: `修改密码失败:${res.message}`
							})
						} else {
							this.update_password_dialog_visible = false
						}
					})
				} else {
					this.$notify.error({
						title: '错误',
						message: `修改密码失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>

<style>
.wx {
	width: 320px;
	height: 320px;
	background: url('@/assets/wx.png');
	background-size: contain;
}
.zfb {
	width: 320px;
	height: 320px;
	background: url('@/assets/zfb.png');
	background-size: contain;
}
.right_menu {
	float: right !important;
	color: white !important;
	line-height: 60px;
	padding-left: 15px !important;
	padding-right: 15px !important;
	font-size: 14px !important;
	font-weight: 400 !important;
}
.right_menu:hover {
	background-color: rgb(42, 58, 78) !important;
}
</style>
