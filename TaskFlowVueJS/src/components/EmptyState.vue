<script setup lang="ts">
import AppIcon from './AppIcon.vue'
defineProps<{ title: string; text?: string; icon?: string }>()
</script>

<template>
  <div class="empty">
    <span class="empty-icon"><AppIcon :name="icon || 'inbox'" :size="28" /></span>
    <h3>{{ title }}</h3>
    <p v-if="text">{{ text }}</p>
    <slot />
  </div>
</template>
