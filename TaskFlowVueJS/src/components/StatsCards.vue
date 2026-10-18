<script setup lang="ts">
import type { Stats } from '../types'
import AppIcon from './AppIcon.vue'

defineProps<{ stats: Stats }>()
</script>

<template>
  <div class="cards">
    <div class="card stat">
      <span class="stat-icon t-green"><AppIcon name="checkCircle" :size="22" /></span>
      <div><strong>{{ stats.completed }}</strong><span>Terminées</span></div>
    </div>
    <div class="card stat">
      <span class="stat-icon t-violet"><AppIcon name="clock" :size="22" /></span>
      <div><strong>{{ stats.inProgress }}</strong><span>En cours</span></div>
    </div>
    <div class="card stat">
      <span class="stat-icon t-red"><AppIcon name="alert" :size="22" /></span>
      <div><strong>{{ stats.overdue }}</strong><span>En retard</span></div>
    </div>
  </div>
</template>
