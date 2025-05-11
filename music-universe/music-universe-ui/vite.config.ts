import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    // 4) alias: чтобы писать import X from '@/components/X'
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  }
})
