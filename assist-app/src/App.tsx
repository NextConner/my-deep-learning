import { useState, useEffect } from 'react';
import { Login } from './components/Login';
import { ChatInterface } from './components/ChatInterface';
import { Watermark } from './components/Watermark';
import { User } from './types';

export default function App() {
  const [enterpriseName, setEnterpriseName] = useState('企业');
  const [user, setUser] = useState<User>({
    username: '',
    isLoggedIn: false,
    tokenUsage: 0,
  });

  useEffect(() => {
    const fetchEnterpriseInfo = async () => {
      try {
        const response = await fetch('/api/auth/enterprise-info');
        if (!response.ok) {
          throw new Error('Failed to load enterprise info');
        }

        const data = await response.json();
        if (typeof data.name === 'string' && data.name.trim()) {
          setEnterpriseName(data.name.trim());
        }
      } catch (error) {
        console.error('Failed to load enterprise info:', error);
      }
    };

    fetchEnterpriseInfo();
  }, []);

  // Load user from local storage if exists
  useEffect(() => {
    const savedUser = localStorage.getItem('ai_assistant_user');
    if (savedUser) {
      const parsed = JSON.parse(savedUser);
      setUser({
        ...parsed,
        tokenUsage: parsed.tokenUsage || 0
      });
    }
  }, []);

  const handleLogin = (username: string, tokenUsage: number, token: string) => {
    const newUser = { username, isLoggedIn: true, tokenUsage, token };
    setUser(newUser);
    localStorage.setItem('ai_assistant_user', JSON.stringify(newUser));
  };

  const handleLogout = () => {
    const loggedOutUser = { username: '', isLoggedIn: false, tokenUsage: 0, token: undefined };
    setUser(loggedOutUser);
    localStorage.removeItem('ai_assistant_user');
  };

  return (
    <div className="min-h-screen bg-slate-100 relative">
      {!user.isLoggedIn ? (
        <Login onLogin={handleLogin} enterpriseName={enterpriseName} />
      ) : (
        <>
          <Watermark text={user.username} />
          <ChatInterface user={user} onLogout={handleLogout} enterpriseName={enterpriseName} />
        </>
      )}
    </div>
  );
}
