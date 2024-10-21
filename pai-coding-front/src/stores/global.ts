// src/stores/global.ts
import { defineStore } from 'pinia';
import axios from 'axios';
import { doGet } from '@/http/BackendRequests';
import { GLOBAL_STORE } from '@/constants/StoreConstants'
import { defaultGlobalResponse, type GlobalResponse } from '@/http/ResponseTypes/CommonResponseType'

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
