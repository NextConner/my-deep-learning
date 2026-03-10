import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import path from 'path';
import {defineConfig, loadEnv} from 'vite';

export default defineConfig(({mode}) => {
  const env = loadEnv(mode, '.', '');
  const apiBase = env.VITE_API_BASE_URL || 'http://localhost:8080';
  return {
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, '.'),
      },
    },
    server: {
      // HMR is disabled in AI Studio via DISABLE_HMR env var.
      // Do not modify — file watching is disabled to prevent flickering during agent edits.
      hmr: process.env.DISABLE_HMR !== 'true',
      proxy: {
        // 将后端接口请求代理到 Java 服务（仅 vite dev 模式生效）
        '/api/auth': { target: apiBase, changeOrigin: true },
        '/api/chat': { target: apiBase, changeOrigin: true },
        '/api/usage': { target: apiBase, changeOrigin: true },
        '/api/document': { target: apiBase, changeOrigin: true },
      },
    },
  };
});
