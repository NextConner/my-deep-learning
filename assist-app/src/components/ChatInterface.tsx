import React, { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Virtuoso, VirtuosoHandle } from 'react-virtuoso';
import { AlertCircle, Send, User, Bot, LogOut, Loader2, PlusCircle, Cpu, Briefcase, Users, CreditCard, MoreHorizontal, ChevronDown, RefreshCcw, Mic, MicOff, Copy, Check, Info, X, History, Trash2, MessageSquare, PanelLeftOpen, PanelLeftClose } from 'lucide-react';
import Markdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { GoogleGenAI } from '@google/genai';
import { Message, Role, User as UserType, Category, ChatSession } from '../types';

const MAX_INPUT_LENGTH = 500;
const MAX_CONTEXT_MESSAGES = 10; // Only send last 10 messages for context
const SENSITIVE_WORDS = ['暴力', '色情', '赌博', '毒品', '脏话', '骂人', '攻击性言论'];

const IconMap: Record<string, React.ReactNode> = {
  Cpu: <Cpu size={14} />,
  Briefcase: <Briefcase size={14} />,
  Users: <Users size={14} />,
  CreditCard: <CreditCard size={14} />,
  MoreHorizontal: <MoreHorizontal size={14} />,
};

interface ChatInterfaceProps {
  user: UserType;
  onLogout: () => void;
}

const CopyButton = ({ text }: { text: string }) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy!', err);
    }
  };

  return (
    <button
      onClick={handleCopy}
      className="p-1.5 text-stone-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-md transition-all active:scale-90"
      title="复制内容"
    >
      {copied ? <Check size={14} className="text-emerald-600" /> : <Copy size={14} />}
    </button>
  );
};

const useLongPress = (callback: () => void, ms = 500) => {
  const [startLongPress, setStartLongPress] = useState(false);
  const timerRef = useRef<any>(null);

  useEffect(() => {
    if (startLongPress) {
      timerRef.current = setTimeout(callback, ms);
    } else {
      clearTimeout(timerRef.current);
    }

    return () => {
      clearTimeout(timerRef.current);
    };
  }, [startLongPress, callback, ms]);

  return {
    onMouseDown: () => setStartLongPress(true),
    onMouseUp: () => setStartLongPress(false),
    onMouseLeave: () => setStartLongPress(false),
    onTouchStart: () => setStartLongPress(true),
    onTouchEnd: () => setStartLongPress(false),
  };
};

const MessageBubble = ({ 
  msg, 
  isLoading, 
  isLast, 
  onLongPress, 
  isCopied,
  onRetry,
  onShowDetails
}: { 
  msg: Message; 
  isLoading: boolean; 
  isLast: boolean; 
  onLongPress: () => void;
  isCopied: boolean;
  onRetry: () => void;
  onShowDetails: () => void;
}) => {
  const longPressHandlers = useLongPress(onLongPress);

  return (
    <div 
      {...longPressHandlers}
      className={`px-2 py-1 rounded-xl w-fit relative group transition-all select-none cursor-pointer active:scale-[0.99] ${
        msg.role === Role.USER 
          ? 'bg-white text-stone-800 rounded-tr-none shadow-sm border border-stone-100 ml-auto' 
          : msg.isError 
            ? 'bg-red-50 border border-red-100 text-red-800 rounded-tl-none'
            : 'bg-slate-50 border border-stone-100 text-stone-800 rounded-tl-none shadow-sm'
      }`}
    >
      <div className={`markdown-body text-sm leading-relaxed ${msg.isError ? 'text-red-700' : ''}`}>
        {msg.isError && <AlertCircle size={14} className="inline-block mr-1 mb-1" />}
        <Markdown 
          remarkPlugins={[remarkGfm]}
          components={{
            code({ node, inline, className, children, ...props }: any) {
              const match = /language-(\w+)/.exec(className || '');
              return !inline && match ? (
                <SyntaxHighlighter
                  style={vscDarkPlus}
                  language={match[1]}
                  PreTag="div"
                  {...props}
                >
                  {String(children).replace(/\n$/, '')}
                </SyntaxHighlighter>
              ) : (
                <code className={className} {...props}>
                  {children}
                </code>
              );
            }
          }}
        >
          {msg.content || (isLoading && isLast ? '...' : '')}
        </Markdown>
      </div>
      
      <div className="absolute top-2 right-2 flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
        {msg.role === Role.ASSISTANT && msg.content && !msg.isError && (
          <CopyButton text={msg.content} />
        )}
        <button
          onClick={(e) => {
            e.stopPropagation();
            onShowDetails();
          }}
          className="p-1.5 text-stone-400 hover:text-stone-600 hover:bg-stone-100 rounded-md transition-all active:scale-90"
          title="显示详情"
        >
          <Info size={14} />
        </button>
      </div>

      <AnimatePresence>
        {isCopied && (
          <motion.div
            initial={{ opacity: 0, scale: 0.8, y: -10 }}
            animate={{ opacity: 1, scale: 1, y: -20 }}
            exit={{ opacity: 0, scale: 0.8, y: -30 }}
            className="absolute -top-4 left-1/2 -translate-x-1/2 bg-stone-800 text-white text-[10px] px-2 py-0.5 rounded-full shadow-lg z-10 font-bold"
          >
            已复制
          </motion.div>
        )}
      </AnimatePresence>
      
      {msg.isError && (
        <button
          onClick={(e) => {
            e.stopPropagation();
            onRetry();
          }}
          className="mt-2 flex items-center gap-1.5 text-[10px] font-bold text-red-600 hover:text-red-700 bg-white px-2 py-1 rounded-lg border border-red-200 shadow-sm transition-all active:scale-95"
        >
          <RefreshCcw size={10} />
          <span>重试生成</span>
        </button>
      )}
    </div>
  );
};

const MessageDetailsModal = ({ msg, onClose }: { msg: Message; onClose: () => void }) => {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center p-4"
      onClick={onClose}
    >
      <motion.div
        initial={{ scale: 0.9, opacity: 0, y: 20 }}
        animate={{ scale: 1, opacity: 1, y: 0 }}
        exit={{ scale: 0.9, opacity: 0, y: 20 }}
        className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="p-4 border-b border-stone-100 flex items-center justify-between bg-stone-50/50">
          <h3 className="font-bold text-stone-800 flex items-center gap-2">
            <Info size={18} className="text-emerald-600" />
            消息详情
          </h3>
          <button
            onClick={onClose}
            className="p-2 hover:bg-stone-200 rounded-full transition-colors"
          >
            <X size={18} className="text-stone-500" />
          </button>
        </div>
        <div className="p-6 space-y-4">
          <div className="space-y-1">
            <label className="text-[10px] font-bold text-stone-400 uppercase tracking-widest">消息 ID</label>
            <div className="p-3 bg-stone-50 rounded-xl border border-stone-100 font-mono text-xs text-stone-600 break-all">
              {msg.id}
            </div>
          </div>
          <div className="space-y-1">
            <label className="text-[10px] font-bold text-stone-400 uppercase tracking-widest">发送时间</label>
            <div className="p-3 bg-stone-50 rounded-xl border border-stone-100 text-sm text-stone-600">
              {new Date(msg.timestamp).toLocaleString('zh-CN', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
              })}
            </div>
          </div>
          <div className="space-y-1">
            <label className="text-[10px] font-bold text-stone-400 uppercase tracking-widest">所属分类</label>
            <div className="p-3 bg-stone-50 rounded-xl border border-stone-100 text-sm text-stone-600 flex items-center gap-2">
              {msg.category ? (
                <>
                  <span className="w-2 h-2 rounded-full bg-emerald-500" />
                  {msg.category}
                </>
              ) : (
                <span className="text-stone-400 italic">未分类</span>
              )}
            </div>
          </div>
          <div className="space-y-1">
            <label className="text-[10px] font-bold text-stone-400 uppercase tracking-widest">角色</label>
            <div className="p-3 bg-stone-50 rounded-xl border border-stone-100 text-sm text-stone-600 flex items-center gap-2">
              {msg.role === Role.USER ? (
                <>
                  <User size={14} className="text-emerald-600" />
                  用户
                </>
              ) : (
                <>
                  <Bot size={14} className="text-blue-600" />
                  AI 助手
                </>
              )}
            </div>
          </div>
        </div>
        <div className="p-4 bg-stone-50 border-t border-stone-100 flex justify-end">
          <button
            onClick={onClose}
            className="px-6 py-2 bg-stone-800 text-white rounded-xl font-bold text-sm hover:bg-stone-700 transition-colors active:scale-95 shadow-lg shadow-stone-200"
          >
            关闭
          </button>
        </div>
      </motion.div>
    </motion.div>
  );
};

export const ChatInterface: React.FC<ChatInterfaceProps> = ({ user, onLogout }) => {
  const [currentTokenUsage, setCurrentTokenUsage] = useState(user.tokenUsage || 0);
  
  // Session management states
  const [sessions, setSessions] = useState<ChatSession[]>([]);
  const [currentSessionId, setCurrentSessionId] = useState<string | null>(null);
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);

  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [validationError, setValidationError] = useState<string | null>(null);
  const [suggestion, setSuggestion] = useState<string | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null);
  const [isCategoryMenuOpen, setIsCategoryMenuOpen] = useState(false);
  const [isRecording, setIsRecording] = useState(false);
  const [copiedMessageId, setCopiedMessageId] = useState<string | null>(null);
  const [detailMessage, setDetailMessage] = useState<Message | null>(null);
  const [isAtBottom, setIsAtBottom] = useState(true);
  const recognitionRef = useRef<any>(null);
  const virtuosoRef = useRef<VirtuosoHandle>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const currentSession = sessions.find(s => s.id === currentSessionId);
  const isLoading = currentSession?.isGenerating || false;

  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${textareaRef.current.scrollHeight}px`;
    }
  }, [input]);

  useEffect(() => {
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (SpeechRecognition) {
      recognitionRef.current = new SpeechRecognition();
      recognitionRef.current.continuous = false;
      recognitionRef.current.interimResults = false;
      recognitionRef.current.lang = 'zh-CN';

      recognitionRef.current.onresult = (event: any) => {
        const transcript = event.results[0][0].transcript;
        setInput(prev => prev + transcript);
        setIsRecording(false);
      };

      recognitionRef.current.onerror = (event: any) => {
        console.error('Speech recognition error', event.error);
        setIsRecording(false);
        if (event.error === 'not-allowed') {
          setValidationError('语音识别权限被拒绝，请检查浏览器设置。');
        } else {
          setValidationError('语音识别出现错误，请重试。');
        }
      };

      recognitionRef.current.onend = () => {
        setIsRecording(false);
      };
    }
  }, []);

  const toggleRecording = () => {
    if (isRecording) {
      recognitionRef.current?.stop();
    } else {
      if (!recognitionRef.current) {
        setValidationError('您的浏览器不支持语音识别功能。');
        return;
      }
      setValidationError(null);
      setIsRecording(true);
      recognitionRef.current.start();
    }
  };

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await fetch('/api/categories');
        const data = await res.json();
        setCategories(data);
        if (data.length > 0) setSelectedCategory(data[0]);
      } catch (err) {
        console.error('Failed to fetch categories:', err);
      }
    };
    fetchCategories();
  }, []);

  const handleLongPressCopy = async (text: string, id: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedMessageId(id);
      setTimeout(() => setCopiedMessageId(null), 2000);
      
      // Haptic feedback if available
      if ('vibrate' in navigator) {
        navigator.vibrate(50);
      }
    } catch (err) {
      console.error('Failed to copy!', err);
    }
  };

  const handleSend = async (retryMessageId?: string) => {
    const messageToProcess = retryMessageId 
      ? messages.find(m => m.id === retryMessageId)
      : null;
    
    const textToSend = messageToProcess ? messageToProcess.content : input;

    if (!textToSend.trim() || isLoading || !currentSessionId) return;

    // Validation: Reply limit (20 assistant replies per session)
    const replyCount = messages.filter(m => m.role === Role.ASSISTANT && m.id !== '1').length;
    if (replyCount >= 20 && !retryMessageId) {
      setValidationError('本轮对话回复已达到20次上限，请开启新对话。');
      setSuggestion('建议：您可以点击左上角的“新对话”按钮开启一轮新的交流。');
      return;
    }

    // Validation: Sensitive words (only for new messages)
    if (!retryMessageId) {
      const foundSensitiveWord = SENSITIVE_WORDS.find(word => textToSend.includes(word));
      if (foundSensitiveWord) {
        setValidationError(`输入内容包含敏感词汇或不当用语，请修改后重试。`);
        setSuggestion(`建议：请尝试使用更中性、专业的词汇替换“${foundSensitiveWord}”，或重新组织您的表达。`);
        return;
      }

      // Validation: Max length
      if (textToSend.length > MAX_INPUT_LENGTH) {
        setValidationError(`输入内容超出最大长度限制 (${MAX_INPUT_LENGTH}字)。`);
        setSuggestion(`建议：您可以尝试精简内容，或将长篇内容拆分为多个简短的问题分次发送。`);
        return;
      }
    }

    setValidationError(null);
    setSuggestion(null);
    
    const activeSessionId = currentSessionId;
    let assistantMessageId: string;
    let updatedMessages: Message[] = [];
    
    if (retryMessageId) {
      const userIdx = messages.findIndex(m => m.id === retryMessageId);
      const nextMsg = messages[userIdx + 1];
      if (nextMsg && nextMsg.role === Role.ASSISTANT) {
        assistantMessageId = nextMsg.id;
        updatedMessages = messages.map(m => 
          m.id === assistantMessageId ? { ...m, content: '', isError: false } : m
        );
      } else {
        return;
      }
    } else {
      const userMessage: Message = {
        id: Date.now().toString(),
        role: Role.USER,
        content: textToSend,
        timestamp: Date.now(),
        category: selectedCategory?.name,
      };

      assistantMessageId = (Date.now() + 1).toString();
      const assistantMessage: Message = {
        id: assistantMessageId,
        role: Role.ASSISTANT,
        content: '',
        timestamp: Date.now(),
      };
      
      updatedMessages = [...messages, userMessage, assistantMessage];
      setInput('');
    }

    // Update both local messages and sessions array
    setMessages(updatedMessages);
    setSessions(prev => prev.map(s => 
      s.id === activeSessionId 
        ? { ...s, messages: updatedMessages, isGenerating: true, lastUpdated: Date.now() } 
        : s
    ));

    try {
      const inputTokens = textToSend.length * 2;
      setCurrentTokenUsage(prev => prev + inputTokens);

      const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });
      
      const contextMessages = retryMessageId 
        ? updatedMessages.slice(0, updatedMessages.findIndex(m => m.id === retryMessageId))
        : updatedMessages.slice(0, -1);
      
      const limitedContext = contextMessages.slice(-MAX_CONTEXT_MESSAGES);

      const response = await ai.models.generateContentStream({
        model: 'gemini-3-flash-preview',
        contents: [
          ...limitedContext.map(m => ({
            role: m.role === Role.USER ? 'user' : 'model',
            parts: [{ text: m.content }]
          })),
          { role: 'user', parts: [{ text: textToSend }] }
        ],
        config: {
          systemInstruction: "你是一个专业、高效的企业内部AI助手。回答要准确、简洁，并符合企业文化。如果涉及敏感信息，请提醒用户遵守公司安全规定。",
        }
      });

      let fullContent = '';
      for await (const chunk of response) {
        const chunkText = chunk.text;
        if (chunkText) {
          fullContent += chunkText;
          setCurrentTokenUsage(prev => prev + (chunkText.length * 3));
          
          setSessions(prev => prev.map(s => {
            if (s.id === activeSessionId) {
              const newMsgs = s.messages.map(msg => 
                msg.id === assistantMessageId ? { ...msg, content: fullContent, isError: false } : msg
              );
              // Only update the UI messages if this session is still the active one
              if (s.id === (window as any).activeSessionIdRef) {
                setMessages(newMsgs);
              }
              return { ...s, messages: newMsgs, lastUpdated: Date.now() };
            }
            return s;
          }));
        }
      }
      
      setSessions(prev => prev.map(s => {
        if (s.id === activeSessionId) {
          const title = s.title === '新会话' 
            ? textToSend.substring(0, 20) || '新会话' 
            : s.title;
          return { ...s, title, isGenerating: false, lastUpdated: Date.now() };
        }
        return s;
      }));

    } catch (error) {
      console.error('AI Error:', error);
      setSessions(prev => prev.map(s => {
        if (s.id === activeSessionId) {
          const newMsgs = s.messages.map(msg => 
            msg.id === assistantMessageId ? { ...msg, content: '抱歉，服务出现异常，请稍后重试。', isError: true } : msg
          );
          if (s.id === (window as any).activeSessionIdRef) {
            setMessages(newMsgs);
          }
          return { ...s, messages: newMsgs, isGenerating: false, lastUpdated: Date.now() };
        }
        return s;
      }));
    }
  };

  // Load sessions from localStorage on mount
  useEffect(() => {
    const savedSessions = localStorage.getItem(`ai_assistant_sessions_${user.username}`);
    if (savedSessions) {
      const parsed = JSON.parse(savedSessions);
      setSessions(parsed);
      
      // Load the last active session or the most recent one
      const lastSessionId = localStorage.getItem(`ai_assistant_last_session_id_${user.username}`);
      if (lastSessionId && parsed.find((s: ChatSession) => s.id === lastSessionId)) {
        setCurrentSessionId(lastSessionId);
        setMessages(parsed.find((s: ChatSession) => s.id === lastSessionId).messages);
      } else if (parsed.length > 0) {
        setCurrentSessionId(parsed[0].id);
        setMessages(parsed[0].messages);
      } else {
        createNewSession(true);
      }
    } else {
      createNewSession(true);
    }
  }, []);

  // Save sessions to localStorage whenever they change
  useEffect(() => {
    if (sessions.length > 0) {
      localStorage.setItem(`ai_assistant_sessions_${user.username}`, JSON.stringify(sessions));
    }
    if (currentSessionId) {
      localStorage.setItem(`ai_assistant_last_session_id_${user.username}`, currentSessionId);
      // Keep a ref to the active session ID for async operations
      (window as any).activeSessionIdRef = currentSessionId;
    }
  }, [sessions, currentSessionId, user.username]);

  const createNewSession = (force = false) => {
    if (!force && sessions.length >= 5) {
      setValidationError('对话记录已达到5个限制，请删除旧的对话记录后再创建新对话。');
      setIsHistoryOpen(true);
      return;
    }
    const newId = Date.now().toString();
    const newSession: ChatSession = {
      id: newId,
      title: '新会话',
      messages: [
        {
          id: '1',
          role: Role.ASSISTANT,
          content: `你好，${user.username}！我是您的企业AI助手。有什么我可以帮您的吗？`,
          timestamp: Date.now(),
        },
      ],
      timestamp: Date.now(),
      lastUpdated: Date.now(),
    };
    setSessions(prev => [newSession, ...prev]);
    setCurrentSessionId(newId);
    setMessages(newSession.messages);
    setIsHistoryOpen(false);
  };

  const switchSession = (id: string) => {
    const session = sessions.find(s => s.id === id);
    if (session) {
      setCurrentSessionId(id);
      setMessages(session.messages);
      setIsHistoryOpen(false);
    }
  };

  const deleteSession = (e: React.MouseEvent, id: string) => {
    e.stopPropagation();
    const updatedSessions = sessions.filter(s => s.id !== id);
    setSessions(updatedSessions);
    
    if (currentSessionId === id) {
      if (updatedSessions.length > 0) {
        switchSession(updatedSessions[0].id);
      } else {
        createNewSession(true);
      }
    }
  };

  const resetChat = () => {
    createNewSession();
  };

  return (
    <div className="flex h-screen bg-[#F8F7F4] overflow-hidden">
      {/* History Sidebar */}
      <AnimatePresence initial={false}>
        {isHistoryOpen && (
          <motion.div
            initial={{ width: 0, opacity: 0 }}
            animate={{ width: 280, opacity: 1 }}
            exit={{ width: 0, opacity: 0 }}
            transition={{ type: 'spring', damping: 25, stiffness: 200 }}
            className="h-full bg-white border-r border-stone-200 flex flex-col overflow-hidden shrink-0 z-20"
          >
            <div className="p-4 border-b border-stone-100 flex items-center justify-between bg-stone-50/50">
              <h2 className="font-bold text-stone-800 flex items-center gap-2">
                <History size={18} className="text-emerald-600" />
                历史对话
              </h2>
              <button
                onClick={() => setIsHistoryOpen(false)}
                className="p-2 hover:bg-stone-200 rounded-full transition-colors lg:hidden"
              >
                <X size={18} className="text-stone-500" />
              </button>
            </div>
            
            <div className="p-3">
              <button
                onClick={() => createNewSession()}
                className={`w-full flex items-center justify-center gap-2 py-2.5 rounded-xl font-bold text-sm transition-all active:scale-95 shadow-lg ${
                  sessions.length >= 5 
                    ? 'bg-stone-100 text-stone-400 cursor-not-allowed shadow-none' 
                    : 'bg-emerald-600 text-white hover:bg-emerald-700 shadow-emerald-100'
                }`}
              >
                <PlusCircle size={16} />
                开启新对话
              </button>
            </div>

            <div className="flex-1 overflow-y-auto p-2 space-y-4 scrollbar-hide">
              {sessions.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-10 text-stone-400 gap-2">
                  <MessageSquare size={32} className="opacity-20" />
                  <p className="text-xs">暂无历史记录</p>
                </div>
              ) : (
                Object.entries(
                  sessions.reduce((groups: { [key: string]: ChatSession[] }, session) => {
                    const date = new Date(session.timestamp).toLocaleDateString('zh-CN', {
                      year: 'numeric',
                      month: '2-digit',
                      day: '2-digit'
                    }).replace(/\//g, '-');
                    if (!groups[date]) groups[date] = [];
                    groups[date].push(session);
                    return groups;
                  }, {})
                ).sort((a, b) => b[0].localeCompare(a[0])).map(([date, dateSessions]) => (
                  <div key={date} className="space-y-1">
                    <div className="px-3 py-1 text-[10px] font-bold text-stone-400 uppercase tracking-wider bg-stone-50/50 rounded-md">
                      {date}
                    </div>
                    {dateSessions.map((session) => (
                      <div
                        key={session.id}
                        onClick={() => switchSession(session.id)}
                        className={`group flex items-center justify-between p-3 rounded-xl cursor-pointer transition-all ${
                          currentSessionId === session.id
                            ? 'bg-emerald-50 text-emerald-700 border border-emerald-100'
                            : 'hover:bg-stone-50 text-stone-600 border border-transparent'
                        }`}
                      >
                        <div className="flex items-center gap-3 overflow-hidden">
                          <div className="relative">
                            <MessageSquare size={16} className={currentSessionId === session.id ? 'text-emerald-600' : 'text-stone-400'} />
                            {session.isGenerating && (
                              <span className="absolute -top-1 -right-1 w-2 h-2 bg-emerald-500 rounded-full animate-pulse" />
                            )}
                          </div>
                          <div className="flex flex-col overflow-hidden">
                            <span className="text-xs font-semibold truncate">{session.title}</span>
                            <span className="text-[10px] opacity-50">
                              {new Date(session.lastUpdated).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                          </div>
                        </div>
                        <button
                          onClick={(e) => deleteSession(e, session.id)}
                          className="p-1.5 text-stone-300 hover:text-red-600 hover:bg-red-50 rounded-md opacity-0 group-hover:opacity-100 transition-all"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    ))}
                  </div>
                ))
              )}
            </div>
            
            <div className="p-4 border-t border-stone-100 text-center">
              <p className={`text-[10px] font-bold uppercase tracking-widest ${sessions.length >= 5 ? 'text-red-500' : 'text-stone-400'}`}>
                会话限制: {sessions.length} / 5
              </p>
              {sessions.length >= 5 && (
                <p className="text-[9px] text-red-400 mt-1">已达上限，请删除旧会话以创建新会话</p>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="flex-1 flex flex-col h-screen relative overflow-hidden bg-white">
        {/* Header */}
        <header className="bg-[#F8F7F4]/80 backdrop-blur-md border-b border-stone-200 px-4 py-3 flex items-center justify-between sticky top-0 z-10">
          <div className="flex items-center gap-3">
            <button 
              onClick={() => setIsHistoryOpen(!isHistoryOpen)}
              className="p-2 text-stone-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors"
              title={isHistoryOpen ? "收起侧边栏" : "展开侧边栏"}
            >
              {isHistoryOpen ? <PanelLeftClose size={20} /> : <PanelLeftOpen size={20} />}
            </button>
            <div className="w-10 h-10 bg-emerald-600 rounded-xl flex items-center justify-center shadow-md shadow-emerald-100">
              <Bot className="text-white w-6 h-6" />
            </div>
            <div>
              <h1 className="text-sm font-semibold text-stone-900">企业AI助手</h1>
              <div className="flex items-center gap-2">
                <div className="flex items-center gap-1">
                  <span className="w-1.5 h-1.5 bg-emerald-500 rounded-full animate-pulse"></span>
                  <span className="text-[10px] text-stone-500 uppercase tracking-wider font-medium">在线</span>
                </div>
                <span className="text-stone-300">|</span>
                <div className="flex items-center gap-1 bg-stone-100 px-1.5 py-0.5 rounded text-[10px] font-mono text-stone-600">
                  <span className="opacity-60">TOKENS:</span>
                  <span className="font-bold text-emerald-600">{(currentTokenUsage || 0).toLocaleString()}</span>
                </div>
                <span className="text-stone-300">|</span>
                <div className="flex items-center gap-1 bg-stone-100 px-1.5 py-0.5 rounded text-[10px] font-mono text-stone-600">
                  <span className="opacity-60">回复:</span>
                  <span className={`font-bold ${messages.filter(m => m.role === Role.ASSISTANT && m.id !== '1').length >= 15 ? 'text-orange-500' : 'text-emerald-600'}`}>
                    {messages.filter(m => m.role === Role.ASSISTANT && m.id !== '1').length}/20
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button 
              onClick={resetChat}
              className={`p-2 rounded-lg transition-colors ${
                sessions.length >= 5 
                  ? 'text-stone-300 cursor-not-allowed' 
                  : 'text-stone-400 hover:text-emerald-600 hover:bg-emerald-50'
              }`}
              title={sessions.length >= 5 ? "会话记录已达上限" : "开启新对话"}
            >
              <PlusCircle size={20} />
            </button>
            <button 
              onClick={onLogout}
              className="p-2 text-stone-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
              title="退出登录"
            >
              <LogOut size={20} />
            </button>
          </div>
        </header>

      {/* Messages */}
      <main className="flex-1 overflow-hidden bg-white relative">
        <Virtuoso
          ref={virtuosoRef}
          data={messages}
          followOutput={(isAtBottom) => (isAtBottom ? 'auto' : false)}
          atBottomStateChange={setIsAtBottom}
          className="scrollbar-hide"
          itemContent={(index, msg) => (
            <div className="px-4 py-2">
              <motion.div
                initial={{ opacity: 0, y: 10, scale: 0.95 }}
                animate={{ opacity: 1, y: 0, scale: 1 }}
                className={`flex flex-col ${msg.role === Role.USER ? 'items-end' : 'items-start'}`}
              >
                {/* Metadata row - aligned with the bubble */}
                <div className={`flex items-center gap-2 mb-1 ${msg.role === Role.USER ? 'mr-10 flex-row' : 'ml-10 flex-row-reverse'}`}>
                  <div className={`text-[10px] opacity-40 font-medium`}>
                    {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </div>
                  {msg.category && (
                    <div className="text-[10px] font-bold uppercase tracking-wider opacity-50 flex items-center gap-1">
                      <span className="bg-emerald-600/10 text-emerald-700 px-1.5 py-0.5 rounded">#{msg.category}</span>
                    </div>
                  )}
                </div>

                {/* Avatar + Bubble row */}
                <div className={`flex gap-2 max-w-[85%] ${msg.role === Role.USER ? 'flex-row-reverse' : 'flex-row'}`}>
                  <div className={`relative w-8 h-8 rounded-lg flex items-center justify-center shrink-0 shadow-sm ${
                    msg.role === Role.USER ? 'bg-emerald-600 text-white' : 'bg-stone-100 text-stone-600'
                  }`}>
                    {msg.role === Role.USER ? (
                      <User size={16} />
                    ) : <Bot size={16} />}
                  </div>
                  
                  <MessageBubble 
                    msg={msg} 
                    isLoading={isLoading} 
                    isLast={index === messages.length - 1}
                    onLongPress={() => handleLongPressCopy(msg.content, msg.id)}
                    isCopied={copiedMessageId === msg.id}
                    onShowDetails={() => setDetailMessage(msg)}
                    onRetry={() => {
                      const userIdx = messages.findIndex(m => {
                        const nextMsg = messages[messages.indexOf(m) + 1];
                        return nextMsg && nextMsg.id === msg.id;
                      });
                      if (userIdx !== -1) {
                        handleSend(messages[userIdx].id);
                      }
                    }}
                  />
                </div>
              </motion.div>
            </div>
          )}
          components={{
            Header: () => (
              <div className="py-4 text-center">
                <button 
                  className="text-[10px] font-bold text-stone-400 hover:text-emerald-600 transition-colors uppercase tracking-widest flex items-center justify-center gap-2 mx-auto"
                  onClick={() => {
                    // In a real app, this would fetch from DB
                    // Here we just show a toast or message
                    setValidationError("已加载全部历史记录");
                    setTimeout(() => setValidationError(null), 2000);
                  }}
                >
                  <RefreshCcw size={10} />
                  查看更早的消息
                </button>
              </div>
            ),
            Footer: () => <div className="h-4" />
          }}
        />
        
        <AnimatePresence>
          {!isAtBottom && (
            <motion.button
              initial={{ opacity: 0, scale: 0.8, y: 10 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.8, y: 10 }}
              onClick={() => {
                virtuosoRef.current?.scrollToIndex({
                  index: messages.length - 1,
                  behavior: 'smooth',
                });
              }}
              className="absolute bottom-4 right-4 w-10 h-10 bg-white border border-stone-200 rounded-full shadow-lg flex items-center justify-center text-stone-500 hover:text-emerald-600 transition-all z-20 active:scale-90"
              title="回到最新消息"
            >
              <ChevronDown size={20} />
            </motion.button>
          )}
        </AnimatePresence>
      </main>

      {/* Input */}
      <footer className="p-4 bg-white border-t border-stone-100 relative">
        <AnimatePresence>
          {isLoading && (
            <motion.div
              initial={{ opacity: 0, y: 5 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 5 }}
              className="absolute -top-10 left-4 flex items-center gap-2 px-3 py-1.5 bg-white/80 backdrop-blur-sm border border-stone-200 rounded-full shadow-sm z-10"
            >
              <div className="flex gap-1">
                <motion.span
                  animate={{ opacity: [0.4, 1, 0.4] }}
                  transition={{ repeat: Infinity, duration: 1.5, delay: 0 }}
                  className="w-1.5 h-1.5 bg-emerald-500 rounded-full"
                />
                <motion.span
                  animate={{ opacity: [0.4, 1, 0.4] }}
                  transition={{ repeat: Infinity, duration: 1.5, delay: 0.2 }}
                  className="w-1.5 h-1.5 bg-emerald-500 rounded-full"
                />
                <motion.span
                  animate={{ opacity: [0.4, 1, 0.4] }}
                  transition={{ repeat: Infinity, duration: 1.5, delay: 0.4 }}
                  className="w-1.5 h-1.5 bg-emerald-500 rounded-full"
                />
              </div>
              <span className="text-[10px] font-bold text-stone-500 uppercase tracking-wider">AI 正在思考...</span>
            </motion.div>
          )}
        </AnimatePresence>

        <AnimatePresence>
          {validationError && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 10 }}
              className="mb-3 p-3 bg-red-50 border border-red-100 rounded-xl flex flex-col gap-2"
            >
              <div className="flex items-center gap-2 text-red-600 text-xs font-semibold">
                <AlertCircle size={14} />
                <span>{validationError}</span>
              </div>
              {suggestion && (
                <div className="text-[11px] text-stone-600 bg-white/50 p-2 rounded-lg border border-red-100/50 italic">
                  {suggestion}
                </div>
              )}
            </motion.div>
          )}
        </AnimatePresence>

        <div className="flex flex-col gap-3">
          {/* Category Selector */}
          <div className="flex items-center gap-2">
            <div className="relative">
              <button
                onClick={() => setIsCategoryMenuOpen(!isCategoryMenuOpen)}
                className="flex items-center gap-1.5 bg-stone-50 hover:bg-stone-100 text-stone-500 px-3 py-1.5 rounded-full text-[11px] font-semibold transition-all border border-stone-200/50 hover:border-stone-300 shadow-sm"
              >
                {selectedCategory ? (
                  <>
                    <span className="text-emerald-600">{IconMap[selectedCategory.icon]}</span>
                    <span>{selectedCategory.name}</span>
                  </>
                ) : (
                  <span>选择分类</span>
                )}
                <ChevronDown size={12} className={`transition-transform duration-200 ${isCategoryMenuOpen ? 'rotate-180' : ''}`} />
              </button>

              <AnimatePresence>
                {isCategoryMenuOpen && (
                  <motion.div
                    initial={{ opacity: 0, scale: 0.95, y: 10 }}
                    animate={{ opacity: 1, scale: 1, y: 0 }}
                    exit={{ opacity: 0, scale: 0.95, y: 10 }}
                    className="absolute bottom-full left-0 mb-2 w-48 bg-white border border-stone-200 rounded-2xl shadow-2xl z-20 overflow-hidden py-1"
                  >
                    {categories.map((cat) => (
                      <button
                        key={cat.id}
                        onClick={() => {
                          setSelectedCategory(cat);
                          setIsCategoryMenuOpen(false);
                        }}
                        className={`w-full flex items-center gap-3 px-4 py-2.5 text-xs hover:bg-stone-50 transition-colors ${
                          selectedCategory?.id === cat.id ? 'text-emerald-600 bg-emerald-50/50 font-bold' : 'text-stone-600'
                        }`}
                      >
                        <span className={selectedCategory?.id === cat.id ? 'text-emerald-600' : 'text-stone-400'}>
                          {IconMap[cat.icon]}
                        </span>
                        <span>{cat.name}</span>
                      </button>
                    ))}
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          </div>

          <div className="flex items-end gap-2">
            <div className="flex-1 relative group">
              <AnimatePresence>
                {isLoading && !input && (
                  <motion.div
                    initial={{ y: 0, opacity: 0 }}
                    animate={{ 
                      y: [0, -2, 0],
                      opacity: 1
                    }}
                    transition={{ 
                      y: {
                        repeat: Infinity,
                        duration: 1.5,
                        ease: "easeInOut"
                      },
                      opacity: { duration: 0.2 }
                    }}
                    className="absolute left-4 top-[15px] text-sm text-stone-400 pointer-events-none flex items-center gap-1"
                  >
                    <span>AI 正在思考</span>
                    <motion.span
                      animate={{ opacity: [0, 1, 0] }}
                      transition={{ repeat: Infinity, duration: 1.5, times: [0, 0.5, 1] }}
                    >...</motion.span>
                  </motion.div>
                )}
              </AnimatePresence>
              <textarea
                ref={textareaRef}
                value={input}
                onChange={(e) => {
                  const val = e.target.value;
                  if (val.length <= MAX_INPUT_LENGTH + 50) {
                    setInput(val);
                    if (validationError) {
                      setValidationError(null);
                      setSuggestion(null);
                    }
                  }
                }}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    handleSend();
                  }
                }}
                placeholder={isLoading ? "" : isRecording ? "正在聆听您的声音..." : "输入您的问题..."}
                className={`w-full bg-stone-50 border rounded-2xl px-4 py-3.5 pr-14 focus:outline-none focus:ring-4 transition-all resize-none max-h-40 min-h-[54px] text-sm overflow-y-auto ${
                  validationError 
                    ? 'border-red-200 focus:ring-red-500/5 focus:border-red-500' 
                    : 'border-stone-200 focus:ring-emerald-500/5 focus:border-emerald-400'
                } ${isRecording ? 'ring-4 ring-emerald-500/10 border-emerald-400 bg-emerald-50/30' : ''}`}
                rows={1}
              />
              <div className={`absolute right-4 bottom-3.5 text-[10px] font-bold tracking-tighter transition-colors ${
                input.length > MAX_INPUT_LENGTH ? 'text-red-500' : 'text-stone-300'
              }`}>
                {input.length}/{MAX_INPUT_LENGTH}
              </div>
            </div>
            
            <div className="flex items-center gap-2 h-[54px]">
              <button
                onClick={toggleRecording}
                className={`w-11 h-11 flex items-center justify-center rounded-xl transition-all shrink-0 ${
                  isRecording 
                    ? 'bg-red-500 text-white animate-pulse shadow-lg shadow-red-200' 
                    : 'bg-stone-100 text-stone-500 hover:bg-stone-200 hover:text-emerald-600'
                }`}
                title={isRecording ? "停止录音" : "语音输入"}
              >
                {isRecording ? <MicOff size={20} /> : <Mic size={20} />}
              </button>

              <button
                onClick={() => handleSend()}
                disabled={!input.trim() || isLoading || input.length > MAX_INPUT_LENGTH}
                className={`w-11 h-11 flex items-center justify-center rounded-xl transition-all shrink-0 ${
                  !input.trim() || isLoading || input.length > MAX_INPUT_LENGTH
                    ? 'text-stone-300 bg-stone-50 border border-stone-100' 
                    : 'text-white bg-emerald-600 hover:bg-emerald-700 shadow-lg shadow-emerald-100 active:scale-95'
                }`}
              >
                {isLoading ? <Loader2 size={20} className="animate-spin" /> : <Send size={20} />}
              </button>
            </div>
          </div>
        </div>
        <p className="text-[10px] text-center text-stone-400 mt-4 font-medium tracking-wide">
          AI 生成内容仅供参考 · 保护公司数据安全
        </p>
      </footer>

      <AnimatePresence>
        {detailMessage && (
          <MessageDetailsModal 
            msg={detailMessage} 
            onClose={() => setDetailMessage(null)} 
          />
        )}
      </AnimatePresence>
    </div>
  </div>
);
};
