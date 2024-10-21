import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import VueDevTools from 'vite-plugin-vue-devtools'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    VueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  optimizeDeps: {
    include: ['md-editor-v3']
  },
  // server: {
  //   proxy: {
  //     '/': {
  //       target: 'http://xuyifei.site:8081/',
  //       changeOrigin: true,
  //       secure: true,
  //       rewrite: (path) => {
  //         // const newPath = path.replace(/^\/api/, '')
  //         // console.log('Rewritten path:', newPath)
  //         return path
  //       }
  //     }
  //   }
  // }
})
