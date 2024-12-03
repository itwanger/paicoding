// src/stores/global.ts
import {createPinia, defineStore, getActivePinia} from 'pinia';
import { GLOBAL_STORE } from '@/constants/StoreConstants'
import { defaultGlobalResponse, type GlobalResponse } from '@/http/ResponseTypes/CommonResponseType'
import {getCurrentInstance} from "vue";

export const useGlobalStore = defineStore(GLOBAL_STORE, {
  state: () => ({
    global: {... defaultGlobalResponse} as GlobalResponse
  }),
  actions: {
    setGlobal(globalResponse: GlobalResponse) {
      // this.global = globalResponse
      Object.assign(this.global, globalResponse)
      // try {
      //   const response = await axios.get('https://jsonplaceholder.typicode.com/todos');
      //   this.todos = response.data;
      // } catch (error) {
      //   console.error('Error fetching todos:', error);
      // }
    },
  },
});

export async function getGlobalStore() {
  // 确保 Pinia 已经被安装
  if (!getActivePinia()) {
    const pinia = createPinia();
    const app = getCurrentInstance()?.appContext.app;
    if (app) {
      app.use(pinia);
    }
  }

  // 获取并返回 auth store
  return useGlobalStore();
}