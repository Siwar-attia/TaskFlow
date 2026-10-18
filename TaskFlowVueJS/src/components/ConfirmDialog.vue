<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import AppIcon from './AppIcon.vue'

withDefaults(defineProps<{ message?: string }>(), { message: 'Êtes-vous sûr ?' })
const emit = defineEmits<{ confirm: []; cancel: [] }>()

const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') emit('cancel') }
onMounted(() => window.addEventListener('keydown', onKey))
onUnmounted(() => window.removeEventListener('keydown', onKey))
</script>

<template>
  <div class="overlay" @click.self="$emit('cancel')">
    <div class="dialog" role="dialog" aria-modal="true">
      <span class="dialog-icon"><AppIcon name="trash" :size="22" /></span>
      <h3>Confirmer la suppression</h3>
      <p>{{ message }}</p>
      <div class="actions">
        <button class="btn btn-outline" data-test="cancel" @click="$emit('cancel')">Annuler</button>
        <button class="btn btn-danger" data-test="confirm" @click="$emit('confirm')">Supprimer</button>
      </div>
    </div>
  </div>
</template>
