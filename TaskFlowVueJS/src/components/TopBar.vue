<script setup lang="ts">
import { computed } from 'vue'
import { auth, logout } from '../services/keycloak'
import { initials } from '../utils/task'
import AppIcon from './AppIcon.vue'

const emit = defineEmits<{ menu: [] }>()
const isAdmin = computed(() => auth.roles.includes('admin'))
</script>

<template>
  <header class="topbar">
    <!-- Gauche : bouton menu + logo (mobile uniquement) -->
    <div class="topbar-left">
      <button class="icon-btn burger" aria-label="Ouvrir le menu" data-test="burger" @click="emit('menu')">
        <AppIcon name="menu" />
      </button>
      <span class="brand-text">TaskFlow</span>
    </div>

    <!-- Droite : utilisateur connecté + déconnexion -->
    <div class="topbar-right">
      <div class="top-user">
        <span class="avatar">{{ initials(auth.username) }}</span>
        <div class="user-info">
          <span class="user-name" data-test="username">{{ auth.username }}</span>
          <span class="user-role">{{ isAdmin ? 'Administrateur' : 'Utilisateur' }}</span>
        </div>
      </div>
      <button class="btn btn-outline btn-logout" data-test="logout" aria-label="Déconnexion" @click="logout">
        <AppIcon name="logout" :size="16" /><span class="logout-text">Déconnexion</span>
      </button>
    </div>
  </header>
</template>
