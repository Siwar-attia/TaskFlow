<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { auth } from '../services/keycloak'
import AppIcon from './AppIcon.vue'


defineProps<{ open: boolean }>()
const emit = defineEmits<{ close: [] }>()

const route = useRoute()

watch(() => route?.fullPath, () => emit('close'))

const isAdmin = computed(() => auth.roles.includes('admin'))
</script>

<template>
  <div v-if="open" class="backdrop" @click="emit('close')" />

  <aside class="sidebar" :class="{ open }">
    <div class="brand">
      <span class="brand-logo"><AppIcon name="checkCircle" :size="20" /></span>
      <span class="brand-text">TaskFlow</span>
      <button class="icon-btn close-btn" aria-label="Fermer le menu" @click="emit('close')"><AppIcon name="x" /></button>
    </div>

    <nav class="nav">
      <p class="nav-title">Menu</p>
      <router-link to="/" class="nav-item c-pink" active-class="" exact-active-class="active">
        <span class="nav-icon"><AppIcon name="dashboard" :size="17" /></span>Tableau de bord
      </router-link>
      <router-link to="/tasks" class="nav-item c-sky" active-class="active">
        <span class="nav-icon"><AppIcon name="tasks" :size="17" /></span>Mes tâches
      </router-link>
      <router-link to="/profile" class="nav-item c-green" active-class="active">
        <span class="nav-icon"><AppIcon name="user" :size="17" /></span>Profil
      </router-link>

      <template v-if="isAdmin">
        <p class="nav-title">Administration</p>
        <router-link to="/admin" class="nav-item c-amber" active-class="active" data-test="admin-link">
          <span class="nav-icon"><AppIcon name="shield" :size="17" /></span>Administration
        </router-link>
      </template>
    </nav>
  </aside>
</template>
