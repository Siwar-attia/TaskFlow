<script setup lang="ts">
import AppIcon from './AppIcon.vue'
// page commence à 0 (comme Spring Data)
defineProps<{ page: number; totalPages: number }>()
defineEmits<{ change: [page: number] }>()
</script>

<template>
  <div v-if="totalPages > 1" class="pagination">
    <button class="btn btn-outline" data-test="prev" :disabled="page === 0" @click="$emit('change', page - 1)">
      <AppIcon name="left" :size="16" /> Précédent
    </button>
    <span class="page-info">Page {{ page + 1 }} / {{ totalPages }}</span>
    <button class="btn btn-outline" data-test="next" :disabled="page >= totalPages - 1" @click="$emit('change', page + 1)">
      Suivant <AppIcon name="right" :size="16" />
    </button>
  </div>
</template>