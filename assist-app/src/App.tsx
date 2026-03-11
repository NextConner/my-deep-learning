import { useState, useEffect } from 'react';
import { Login } from './components/Login';
import { ChatInterface } from './components/ChatInterface';
import { Watermark } from './components/Watermark';
import { User } from './types';

export default function App() {
  const [user, setUser] = useState<User>({
    username: '',
    isLoggedIn: false,
    tokenUsage: 0,
  });

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
        <Login onLogin={handleLogin} />
      ) : (
        <>
          <Watermark text={user.username} />
          <ChatInterface user={user} onLogout={handleLogout} />
        </>
      )}
    </div>
  );
}
