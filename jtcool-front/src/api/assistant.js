import { getToken } from '@/utils/auth'

const assistBase = (import.meta.env.VITE_APP_ASSIST_API || '/assist-api').replace(/\/$/, '')

function normalizePath(path) {
  return path.startsWith('/') ? path : `/${path}`
}

function createUrl(path, params = {}) {
  const normalizedPath = normalizePath(path)
  const isAbsolute = /^https?:\/\//.test(assistBase)
  const url = new URL(
    isAbsolute ? `${assistBase}${normalizedPath}` : `${window.location.origin}${assistBase}${normalizedPath}`
  )
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, value)
    }
  })
  return isAbsolute ? url.toString() : `${url.pathname}${url.search}`
}

async function requestJson(path, options = {}) {
  const token = getToken()
  const headers = {
    ...(options.headers || {})
  }
  if (token && headers.Authorization === undefined) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(createUrl(path), {
    method: options.method || 'GET',
    headers,
    body: options.body
  })

  const data = await response.json().catch(() => ({}))
  if (!response.ok) {
    const error = new Error(data.error || data.msg || `Request failed: ${response.status}`)
    error.status = response.status
    error.data = data
    throw error
  }
  return data
}

export function getAssistantEnterpriseInfo() {
  return requestJson('/api/auth/enterprise-info')
}

export function getAssistantUsage() {
  return requestJson('/api/usage')
}

export function getQuestionSuggestions(routeContext) {
  return requestJson('/api/suggestions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(routeContext)
  })
}

export async function streamAssistantChat(message, handlers = {}) {
  const token = getToken()
  const response = await fetch(createUrl('/api/chat/stream', {
    message,
    token
  }), {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })

  if (!response.ok) {
    const error = new Error(`Request failed: ${response.status}`)
    error.status = response.status
    throw error
  }

  const reader = response.body?.getReader()
  if (!reader) {
    throw new Error('响应流不可用')
  }

  const decoder = new TextDecoder()
  let buffer = ''
  let eventName = ''
  let dataLines = []

  const dispatchEvent = () => {
    if (!eventName) {
      dataLines = []
      return
    }
    const payload = dataLines.join('\n')
    const handler = handlers[eventName]
    if (handler) {
      handler(payload)
    }
    dataLines = []
    eventName = ''
  }

  while (true) {
    const { done, value } = await reader.read()
    if (done) {
      dispatchEvent()
      break
    }

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split(/\r?\n/)
    buffer = lines.pop() || ''

    lines.forEach((line) => {
      if (!line.trim()) {
        dispatchEvent()
        return
      }
      if (line.startsWith('event:')) {
        eventName = line.slice(6).trim()
        return
      }
      if (line.startsWith('data:')) {
        dataLines.push(line.slice(5).trimStart())
      }
    })
  }
}
