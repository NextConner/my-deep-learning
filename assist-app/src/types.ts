export enum Role {
  USER = 'user',
  ASSISTANT = 'assistant'
}

export interface Message {
  id: string;
  role: Role;
  content: string;
  timestamp: number;
  category?: string;
  isError?: boolean;
}

export interface Category {
  id: string;
  name: string;
  icon: string;
}

export interface User {
  username: string;
  isLoggedIn: boolean;
  tokenUsage: number;
}

export interface ChatSession {
  id: string;
  title: string;
  messages: Message[];
  timestamp: number;
  lastUpdated: number;
  isGenerating?: boolean;
}
