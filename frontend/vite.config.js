import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  // sockjs-client assumes a Node-style `global` exists; Vite doesn't
  // polyfill this by default, so alias it to the browser's globalThis.
  define: {
    global: 'globalThis',
  },
  server: {
    port: 5173,
  },
  build: {
    // Electron loads this as a file:// build, so keep asset paths relative
    outDir: 'dist',
    assetsDir: 'assets',
  },
  base: './',
});
