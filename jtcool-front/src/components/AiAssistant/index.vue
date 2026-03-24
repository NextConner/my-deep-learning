<template>
  <div class="ai-assistant">
    <transition name="assistant-fade">
      <button
        v-if="!drawerVisible"
        class="assistant-trigger"
        type="button"
        @click="openDrawer"
      >
        <el-icon><Service /></el-icon>
        <span>AI助手</span>
      </button>
    </transition>

    <el-drawer
      v-model="drawerVisible"
      append-to-body
      :with-header="false"
      :size="drawerSize"
      custom-class="assistant-drawer"
      destroy-on-close="false"
    >
      <div class="assistant-shell">
        <aside class="assistant-sidebar">
          <div class="assistant-brand">
            <div class="assistant-brand__icon">
              <el-icon><Service /></el-icon>
            </div>
            <div>
              <div class="assistant-brand__title">{{ assistantName }}</div>
              <div class="assistant-brand__meta">{{ usageSummary }}</div>
            </div>
          </div>

          <div class="assistant-actions">
            <el-button type="primary" plain @click="createSession" :disabled="sessions.length >= maxSessions">
              <el-icon><Plus /></el-icon>
              新对话
            </el-button>
            <el-button text @click="refreshMeta" :loading="metaLoading">刷新</el-button>
          </div>

          <div class="assistant-sessions">
            <button
              v-for="session in sessions"
              :key="session.id"
              class="assistant-session"
              :class="{ 'is-active': currentSessionId === session.id }"
              type="button"
              @click="selectSession(session.id)"
            >
              <div class="assistant-session__content">
                <div class="assistant-session__title">{{ session.title }}</div>
                <div class="assistant-session__time">{{ formatTime(session.lastUpdated) }}</div>
              </div>
              <el-button text class="assistant-session__delete" @click.stop="deleteSession(session.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </button>
          </div>
        </aside>

        <section class="assistant-main">
          <header class="assistant-header">
            <div>
              <div class="assistant-header__title">{{ assistantName }}</div>
              <div class="assistant-header__subtitle">{{ userStore.name || '当前用户' }}</div>
            </div>
            <el-button text @click="drawerVisible = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </header>

          <div v-if="suggestedQuestions.length" class="assistant-suggestions">
            <div class="assistant-suggestions__title">您可能想问：</div>
            <el-tag
              v-for="(q, idx) in suggestedQuestions"
              :key="idx"
              @click="askQuestion(q)"
              class="suggestion-tag"
              type="info"
            >
              {{ q }}
            </el-tag>
          </div>

          <div ref="messagesRef" class="assistant-messages">
            <div v-if="!currentMessages.length" class="assistant-empty">
              <div class="assistant-empty__title">开始一段新的对话</div>
              <div class="assistant-empty__desc">输入问题后，助手会在当前会话中持续记录上下文。</div>
            </div>
            <div
              v-for="message in currentMessages"
              :key="message.id"
              class="assistant-message"
              :class="`is-${message.role}`"
            >
              <div class="assistant-message__meta">
                <span>{{ message.role === 'user' ? '你' : '助手' }}</span>
                <span>{{ formatTime(message.timestamp) }}</span>
                <span v-if="message.category">{{ message.category }}</span>
              </div>
              <div class="assistant-message__bubble">
                <div class="assistant-message__text" v-text="message.content || (message.pending ? '...' : '')" />
                <div class="assistant-message__actions">
                  <el-button text @click="showMessageDetail(message)">详情</el-button>
                  <el-button
                    v-if="message.role === 'assistant' && message.content"
                    text
                    @click="copyMessage(message.content)"
                  >
                    复制
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="errorMessage" class="assistant-alert">
            <el-icon><WarningFilled /></el-icon>
            <span>{{ errorMessage }}</span>
          </div>

          <footer class="assistant-footer">
            <div class="assistant-toolbar">
              <el-select v-model="selectedCategory" size="small" class="assistant-category">
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.name"
                />
              </el-select>
              <el-button
                v-if="speechSupported"
                text
                :type="isRecording ? 'danger' : 'default'"
                @click="toggleRecording"
              >
                <el-icon><Microphone /></el-icon>
                {{ isRecording ? '停止语音' : '语音输入' }}
              </el-button>
            </div>

            <el-input
              v-model="input"
              type="textarea"
              :rows="3"
              resize="none"
              maxlength="500"
              show-word-limit
              placeholder="请输入问题，Enter 发送，Shift + Enter 换行"
              @keydown="handleKeydown"
            />

            <div class="assistant-submit">
              <div class="assistant-submit__hint">{{ currentSession ? currentSession.messages.filter(item => item.role === 'assistant').length : 0 }}/20 回复</div>
              <el-button type="primary" :loading="sending" @click="sendMessage">发送</el-button>
            </div>
          </footer>
        </section>
      </div>
    </el-drawer>

    <el-dialog v-model="detailVisible" title="消息详情" width="420px" append-to-body>
      <div v-if="detailMessage" class="assistant-detail">
        <div><strong>ID：</strong>{{ detailMessage.id }}</div>
        <div><strong>角色：</strong>{{ detailMessage.role === 'user' ? '用户' : '助手' }}</div>
        <div><strong>时间：</strong>{{ formatDateTime(detailMessage.timestamp) }}</div>
        <div><strong>分类：</strong>{{ detailMessage.category || '未分类' }}</div>
        <div class="assistant-detail__content"><strong>内容：</strong>{{ detailMessage.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Close, Delete, Microphone, Plus, Service, WarningFilled } from '@element-plus/icons-vue'
import cache from '@/plugins/cache'
import useUserStore from '@/store/modules/user'
import { getAssistantEnterpriseInfo, getAssistantUsage, streamAssistantChat, getQuestionSuggestions } from '@/api/assistant'

const route = useRoute()
const userStore = useUserStore()
const drawerVisible = ref(false)
const sending = ref(false)
const metaLoading = ref(false)
const enterpriseName = ref('企业')
const usageSummary = ref('今日用量待同步')
const input = ref('')
const errorMessage = ref('')
const messagesRef = ref(null)
const detailVisible = ref(false)
const detailMessage = ref(null)
const speechSupported = ref(false)
const isRecording = ref(false)
const recognitionRef = ref(null)
const suggestedQuestions = ref([])

const categories = [
  { id: 'tech', name: '技术咨询' },
  { id: 'business', name: '业务相关' },
  { id: 'team', name: '团队协作' },
  { id: 'finance', name: '财务管理' },
  { id: 'other', name: '其他' }
]

const maxSessions = 5
const sessions = ref([])
const currentSessionId = ref('')
const selectedCategory = ref(categories[0].name)

const assistantName = computed(() => `${enterpriseName.value}AI助手`)
const drawerSize = computed(() => window.innerWidth < 768 ? '100%' : '960px')
const currentSession = computed(() => sessions.value.find(item => item.id === currentSessionId.value) || null)
const currentMessages = computed(() => currentSession.value?.messages || [])
const sessionStorageKey = computed(() => `ai_assistant_sessions_${userStore.id || userStore.name || 'anonymous'}`)
const lastSessionKey = computed(() => `ai_assistant_last_session_${userStore.id || userStore.name || 'anonymous'}`)

const sensitiveWords = ['暴力', '色情', '赌博', '毒品', '脏话', '骂人', '攻击性言论']

watch(sessions, persistSessions, { deep: true })
watch(currentSessionId, () => {
  cache.local.set(lastSessionKey.value, currentSessionId.value)
  nextTick(scrollToBottom)
})

onMounted(() => {
  initSpeechRecognition()
  restoreSessions()
  refreshMeta()
})

function initSpeechRecognition() {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  speechSupported.value = Boolean(SpeechRecognition)
  if (!SpeechRecognition) {
    return
  }
  const recognition = new SpeechRecognition()
  recognition.lang = 'zh-CN'
  recognition.continuous = false
  recognition.interimResults = false
  recognition.onresult = (event) => {
    input.value = `${input.value}${event.results[0][0].transcript}`
  }
  recognition.onend = () => {
    isRecording.value = false
  }
  recognition.onerror = () => {
    isRecording.value = false
    ElMessage.warning('语音识别不可用，请检查浏览器权限')
  }
  recognitionRef.value = recognition
}

function toggleRecording() {
  if (!recognitionRef.value) {
    return
  }
  if (isRecording.value) {
    recognitionRef.value.stop()
    return
  }
  isRecording.value = true
  recognitionRef.value.start()
}

function openDrawer() {
  drawerVisible.value = true
  if (!sessions.value.length) {
    createSession()
  }
  fetchQuestionSuggestions()
  nextTick(scrollToBottom)
}

function createSession() {
  if (sessions.value.length >= maxSessions) {
    ElMessage.warning(`最多保留 ${maxSessions} 个会话`)
    return
  }
  const sessionId = `${Date.now()}`
  const session = {
    id: sessionId,
    title: '新对话',
    messages: [],
    timestamp: Date.now(),
    lastUpdated: Date.now()
  }
  sessions.value = [session, ...sessions.value]
  currentSessionId.value = sessionId
}

function selectSession(sessionId) {
  currentSessionId.value = sessionId
}

function deleteSession(sessionId) {
  if (sessions.value.length === 1) {
    sessions.value = []
    currentSessionId.value = ''
    createSession()
    return
  }
  sessions.value = sessions.value.filter(item => item.id !== sessionId)
  if (currentSessionId.value === sessionId) {
    currentSessionId.value = sessions.value[0]?.id || ''
  }
}

function restoreSessions() {
  const savedSessions = cache.local.getJSON(sessionStorageKey.value)
  const lastSessionId = cache.local.get(lastSessionKey.value)
  if (Array.isArray(savedSessions) && savedSessions.length) {
    sessions.value = savedSessions
    currentSessionId.value = savedSessions.some(item => item.id === lastSessionId) ? lastSessionId : savedSessions[0].id
    return
  }
  createSession()
}

function persistSessions() {
  cache.local.setJSON(sessionStorageKey.value, sessions.value)
}

async function refreshMeta() {
  metaLoading.value = true
  try {
    const [enterprise, usage] = await Promise.all([
      getAssistantEnterpriseInfo(),
      getAssistantUsage().catch(() => null)
    ])
    enterpriseName.value = enterprise?.name || '企业'
    usageSummary.value = usage?.summary || '今日用量待同步'
  } catch (error) {
    console.error('Failed to refresh meta:', error)
  } finally {
    metaLoading.value = false
  }
}

function detectModule(path) {
  if (path.includes('/oms/')) return 'OMS'
  if (path.includes('/wms/')) return 'WMS'
  if (path.includes('/product/')) return 'PRODUCT'
  return 'GENERAL'
}

async function fetchQuestionSuggestions() {
  try {
    const routeContext = {
      path: route.path,
      name: route.name,
      module: detectModule(route.path)
    }
    const response = await getQuestionSuggestions(routeContext)
    suggestedQuestions.value = response.suggestions || []
  } catch (error) {
    console.error('Failed to fetch suggestions:', error)
  }
}

function askQuestion(question) {
  input.value = question
  sendMessage()
}
    ])
    enterpriseName.value = enterprise.name || enterpriseName.value
    usageSummary.value = usage?.summary || '今日用量待同步'
  } catch (error) {
    usageSummary.value = '助手服务未连接'
  } finally {
    metaLoading.value = false
  }
}

function updateCurrentSession(updater) {
  sessions.value = sessions.value.map((session) => {
    if (session.id !== currentSessionId.value) {
      return session
    }
    return updater({
      ...session,
      messages: [...session.messages]
    })
  })
}

function validateInput(text) {
  if (!text.trim()) {
    return '请输入问题内容'
  }
  if (text.length > 500) {
    return '输入内容不能超过 500 字'
  }
  if (sensitiveWords.some(word => text.includes(word))) {
    return '输入内容包含敏感词，请调整后重试'
  }
  if (currentSession.value && currentSession.value.messages.filter(item => item.role === 'assistant').length >= 20) {
    return '当前会话回复已达 20 条，请开启新对话'
  }
  return ''
}

async function sendMessage() {
  if (sending.value) {
    return
  }
  if (!currentSession.value) {
    createSession()
  }

  const content = input.value.trim()
  const validation = validateInput(content)
  if (validation) {
    errorMessage.value = validation
    return
  }

  errorMessage.value = ''
  sending.value = true
  input.value = ''
  drawerVisible.value = true

  const userMessage = {
    id: `${Date.now()}`,
    role: 'user',
    content,
    timestamp: Date.now(),
    category: selectedCategory.value
  }
  const assistantMessage = {
    id: `${Date.now()}-assistant`,
    role: 'assistant',
    content: '',
    timestamp: Date.now(),
    pending: true
  }

  updateCurrentSession((session) => ({
    ...session,
    title: session.title === '新对话' ? content.slice(0, 16) : session.title,
    lastUpdated: Date.now(),
    messages: [...session.messages, userMessage, assistantMessage]
  }))

  nextTick(scrollToBottom)

  try {
    await streamAssistantChat(content, {
      token: (payload) => {
        updateAssistantMessage(assistantMessage.id, (message) => ({
          ...message,
          pending: false,
          content: `${message.content}${payload}`
        }))
        nextTick(scrollToBottom)
      },
      complete: () => {
        updateAssistantMessage(assistantMessage.id, (message) => ({
          ...message,
          pending: false
        }))
      },
      error: (payload) => {
        const parsed = safeParse(payload)
        throw new Error(parsed.message || parsed.error || '助手响应失败')
      }
    })
    await refreshMeta()
  } catch (error) {
    updateAssistantMessage(assistantMessage.id, () => ({
      ...assistantMessage,
      pending: false,
      content: error.message || '助手服务异常，请稍后重试',
      isError: true
    }))
    errorMessage.value = error.message || '助手服务异常，请稍后重试'
    if (error.status === 401 || error.status === 403) {
      await userStore.logOut()
      window.location.href = '/index'
    }
  } finally {
    sending.value = false
    nextTick(scrollToBottom)
  }
}

function updateAssistantMessage(messageId, updater) {
  updateCurrentSession((session) => ({
    ...session,
    lastUpdated: Date.now(),
    messages: session.messages.map(message => message.id === messageId ? updater(message) : message)
  }))
}

function showMessageDetail(message) {
  detailMessage.value = message
  detailVisible.value = true
}

async function copyMessage(content) {
  await navigator.clipboard.writeText(content)
  ElMessage.success('已复制到剪贴板')
}

function handleKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

function scrollToBottom() {
  const container = messagesRef.value
  if (!container) {
    return
  }
  container.scrollTop = container.scrollHeight
}

function formatTime(value) {
  return new Date(value).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function formatDateTime(value) {
  return new Date(value).toLocaleString('zh-CN')
}

function safeParse(payload) {
  try {
    return JSON.parse(payload)
  } catch (error) {
    return { message: payload }
  }
}
</script>

<style lang="scss">
.assistant-drawer {
  padding: 0 !important;
}
</style>

<style lang="scss" scoped>
.ai-assistant {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1200;
}

.assistant-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 18px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #0f766e, #14b8a6);
  color: #fff;
  box-shadow: 0 18px 38px rgba(15, 118, 110, 0.28);
  cursor: pointer;
}

.assistant-shell {
  display: flex;
  height: 100%;
  background: #f5f7fa;
}

.assistant-sidebar {
  width: 280px;
  padding: 20px 16px;
  border-right: 1px solid #e5e7eb;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.assistant-brand {
  display: flex;
  gap: 12px;
  align-items: center;
}

.assistant-brand__icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #0f766e, #14b8a6);
  color: #fff;
  font-size: 20px;
}

.assistant-brand__title,
.assistant-header__title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.assistant-brand__meta,
.assistant-header__subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
}

.assistant-actions {
  display: flex;
  gap: 8px;
}

.assistant-sessions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: auto;
}

.assistant-session {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  background: #f8fafc;
  cursor: pointer;
  text-align: left;
}

.assistant-session.is-active {
  border-color: #14b8a6;
  background: rgba(20, 184, 166, 0.08);
}

.assistant-session__content {
  overflow: hidden;
}

.assistant-session__title {
  overflow: hidden;
  font-size: 13px;
  font-weight: 600;
  color: #111827;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.assistant-session__time {
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
}

.assistant-session__delete {
  flex: none;
}

.assistant-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.assistant-header,
.assistant-footer {
  padding: 18px 20px;
  background: #fff;
}

.assistant-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e5e7eb;
}

.assistant-suggestions {
  padding: 12px 20px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.assistant-suggestions__title {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.suggestion-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}

.suggestion-tag:hover {
  opacity: 0.8;
}

.assistant-messages {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.assistant-empty {
  display: flex;
  height: 100%;
  min-height: 220px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

.assistant-empty__title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.assistant-empty__desc {
  margin-top: 8px;
  font-size: 13px;
}

.assistant-message + .assistant-message {
  margin-top: 16px;
}

.assistant-message__meta {
  margin-bottom: 6px;
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #6b7280;
}

.assistant-message__bubble {
  max-width: 85%;
  padding: 14px 16px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
}

.assistant-message.is-user {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.assistant-message.is-user .assistant-message__bubble {
  background: linear-gradient(135deg, #0f766e, #14b8a6);
  color: #fff;
}

.assistant-message__text {
  white-space: pre-wrap;
  line-height: 1.7;
  word-break: break-word;
}

.assistant-message__actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.assistant-alert {
  margin: 0 20px;
  padding: 10px 12px;
  display: flex;
  gap: 8px;
  align-items: center;
  border-radius: 12px;
  background: #fff7ed;
  color: #c2410c;
}

.assistant-footer {
  border-top: 1px solid #e5e7eb;
}

.assistant-toolbar,
.assistant-submit {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.assistant-toolbar {
  margin-bottom: 12px;
}

.assistant-category {
  width: 180px;
}

.assistant-submit {
  margin-top: 12px;
}

.assistant-submit__hint {
  font-size: 12px;
  color: #6b7280;
}

.assistant-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
  line-height: 1.7;
}

.assistant-detail__content {
  white-space: pre-wrap;
  word-break: break-word;
}

.assistant-fade-enter-active,
.assistant-fade-leave-active {
  transition: opacity 0.2s ease;
}

.assistant-fade-enter-from,
.assistant-fade-leave-to {
  opacity: 0;
}

@media (max-width: 767px) {
  .ai-assistant {
    right: 16px;
    bottom: 16px;
  }

  .assistant-shell {
    flex-direction: column;
  }

  .assistant-sidebar {
    width: 100%;
    max-height: 220px;
    border-right: 0;
    border-bottom: 1px solid #e5e7eb;
  }

  .assistant-message__bubble {
    max-width: 100%;
  }
}
</style>
