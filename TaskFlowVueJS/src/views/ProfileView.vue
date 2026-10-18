<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '../services/api'
import { initials } from '../utils/task'
import type { Me } from '../types'
import AppIcon from '../components/AppIcon.vue'

const me = ref<Me | null>(null)
const error = ref('')

onMounted(async () => {
  try { me.value = await api.me() } catch (e) { error.value = (e as Error).message }
})
</script>

<template>
  <div class="page-head">
    <div>
      <h1>Profil</h1>
      <p class="subtitle">Vos informations de connexion.</p>
    </div>
  </div>

  <p v-if="error" class="alert-error">{{ error }}</p>

  <div v-if="me" class="card profile">
    <span class="avatar avatar-lg">{{ initials(me.name || me.username) }}</span>
    <h2>{{ me.name || me.username }}</h2>
    <p class="muted"><AppIcon name="mail" :size="15" /> {{ me.email }}</p>
    <div class="chips center">
      <span v-for="r in me.roles" :key="r" class="chip chip-role"><AppIcon name="shield" :size="13" /> {{ r }}</span>
    </div>
  </div>
  <div v-else-if="!error" class="skeleton-list"><div class="skeleton" /></div>
</template>
