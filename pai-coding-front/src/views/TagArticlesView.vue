<template>
  <HeaderBar></HeaderBar>

  <div class="home mt-2">

  </div>

  <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
</template>


<script setup lang="ts">

import HeaderBar from '@/components/layout/HeaderBar.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import { provide, ref } from 'vue'

// 登录框
const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
  console.log("clicked: ", loginDialogClicked.value)
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)
</script>

<style scoped>

</style>
