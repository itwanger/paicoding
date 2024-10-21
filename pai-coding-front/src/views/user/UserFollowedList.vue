<template>
  <div class="flex flex-col" v-if="user.records.length > 0">
    <div v-for="userItem in user.records" :key="userItem.userId" class="mb-2">
      <UserFollowedCard :user="userItem"></UserFollowedCard>
    </div>
  </div>
  <div v-else>
    暂时为空~
  </div>
</template>

<script setup lang="ts">
import type { BasicPageType } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { FollowUserInfoType } from '@/http/ResponseTypes/UserInfoType/FollowUserInfoType'
import UserFollowedCard from '@/components/user/UserFollowedCard.vue'

const props = defineProps<{
  user: BasicPageType<FollowUserInfoType>
}>()


</script>



<style scoped>

</style>
