<template>
  <div class="login">
    <div class="login-wrapper">
      <!-- 顶部 JTCOOL ASCII 动画 -->
      <div class="ascii-container">
        <pre class="ascii-art">
      __ ______  ______ ____   ____   __ 
     / //_  __/ / ____// __ \ / __ \ / / 
    / /  / /   / /    / / / // / / // /  
 __/ /  / /   / /___ / /_/ // /_/ // /___
/___/  /_/    \____/ \____/ \____//_____/
        </pre>
      </div>

      <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
        <h3 class="title">{{ title }}</h3>
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            type="text"
            size="large"
            auto-complete="off"
            placeholder="账号"
          >
            <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            auto-complete="off"
            placeholder="密码"
            @keyup.enter="handleLogin"
          >
            <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code" v-if="captchaEnabled">
          <el-input
            v-model="loginForm.code"
            size="large"
            auto-complete="off"
            placeholder="验证码"
            style="width: 63%"
            @keyup.enter="handleLogin"
          >
            <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
          </el-input>
          <div class="login-code">
            <img :src="codeUrl" @click="getCode" class="login-code-img"/>
          </div>
        </el-form-item>
        <el-checkbox v-model="loginForm.rememberMe" class="remember-me">记住密码</el-checkbox>
        <el-form-item style="width:100%;">
          <el-button
            :loading="loading"
            size="large"
            type="primary"
            class="login-btn"
            @click.prevent="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
          <div style="float: right;" v-if="register">
            <router-link class="link-type" :to="'/register'">立即注册</router-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
// 验证码开关
const captchaEnabled = ref(true)
// 注册开关
const register = ref(false)
const redirect = ref(undefined)

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        // 重新获取验证码
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

getCode()
getCookie()
</script>

<style lang='scss' scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  /* 基础护眼浅灰蓝 */
  background-color: #f4f6f9; 
  /* 现代高级的柔和网格渐变（Mesh Gradient），包含浅蓝、浅紫、浅薄荷绿，既丰富又极其护眼 */
  background-image: 
    radial-gradient(at 10% 20%, rgba(226, 236, 255, 0.8) 0px, transparent 50%),
    radial-gradient(at 80% 10%, rgba(234, 224, 255, 0.8) 0px, transparent 50%),
    radial-gradient(at 20% 80%, rgba(220, 245, 235, 0.8) 0px, transparent 50%),
    radial-gradient(at 80% 90%, rgba(245, 225, 240, 0.8) 0px, transparent 50%);
  position: relative;
  overflow: hidden;
}

.login-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: fadeIn 0.8s ease-out;
}

/* ASCII 动画区域 */
.ascii-container {
  margin-bottom: 40px;
  text-align: center;
  width: 100vw;
  display: flex;
  justify-content: center;
}

.ascii-art {
  font-family: 'Courier New', Courier, monospace;
  font-size: 21px; /* 缩小一倍 */
  line-height: 1.1;
  /* 柔和的渐变色 */
  background: linear-gradient(90deg, #8b5cf6, #3b82f6, #10b981, #8b5cf6);
  background-size: 300% 100%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradientWave 4s linear infinite;
  font-weight: bold;
}

@keyframes gradientWave {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.title {
  margin: 0px auto 35px auto;
  text-align: center;
  color: #334155;
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 2px;
}

.login-form {
  border-radius: 24px;
  background: #ffffff;
  /* 柔和的高级阴影 */
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.08), 0 0 10px rgba(0,0,0,0.02);
  width: 420px;
  padding: 45px 35px 30px 35px;
  
  :deep(.el-input__wrapper) {
    background: #f8fafc;
    box-shadow: none;
    border-radius: 12px;
    border: 1px solid #e2e8f0;
    transition: all 0.3s;
    
    &:hover, &.is-focus {
      background: #ffffff;
      border-color: #8b5cf6;
      box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1);
    }
  }

  .el-input {
    height: 48px;
    input {
      height: 48px;
      font-size: 15px;
      color: #334155;
    }
  }
  .input-icon {
    height: 48px;
    width: 18px;
    margin-left: 2px;
    color: #94a3b8;
  }
}

.remember-me {
  margin: 0px 0px 25px 0px;
  :deep(.el-checkbox__label) {
    color: #64748b;
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  letter-spacing: 4px;
  font-weight: bold;
  background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%);
  border: none;
  box-shadow: 0 6px 16px rgba(139, 92, 246, 0.3);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(139, 92, 246, 0.4);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}

.login-code {
  width: 33%;
  height: 48px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
    height: 100%;
    width: 100%;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #94a3b8;
  font-family: Arial, sans-serif;
  font-size: 13px;
  letter-spacing: 1px;
  z-index: 1;
}

.login-code-img {
  height: 48px;
}
</style>