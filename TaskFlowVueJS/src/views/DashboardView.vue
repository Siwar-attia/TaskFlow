<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { api } from '../services/api'
import { auth } from '../services/keycloak'
import { dueLabel, isOverdue, normalizeStats, priorityLabel } from '../utils/task'
import type { Stats, Task } from '../types'
import StatsCards from '../components/StatsCards.vue'
import EmptyState from '../components/EmptyState.vue'
import AppIcon from '../components/AppIcon.vue'

const stats = ref<Stats>(normalizeStats())
const upcoming = ref<Task[]>([])
const error = ref('')
const loading = ref(true)

const firstName = computed(() => auth.username.split(' ')[0] || '')

onMounted(async () => {
  try {
    stats.value = normalizeStats(await api.stats())
    // Tâches non terminées triées par échéance : on garde les 5 premières avec une date
    const page = await api.tasks({ termine: false, sort: 'dateEcheance,asc', size: 50 })
    upcoming.value = page.content.filter((t) => t.dateEcheance).slice(0, 5)
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-head">
    <div>
      <h1>Bonjour {{ firstName }}</h1>
      <p class="subtitle">Voici un aperçu de vos tâches.</p>
    </div>
    <router-link to="/tasks/new" class="btn"><AppIcon name="plus" :size="18" /> Nouvelle tâche</router-link>
  </div>

  <p v-if="error" class="alert-error">{{ error }}</p>

  <StatsCards :stats="stats" />

  <div class="section-head">
    <h2>5 prochaines échéances</h2>
    <router-link to="/tasks" class="link">Tout voir →</router-link>
  </div>

  <div v-if="loading" class="skeleton-list"><div class="skeleton" v-for="i in 3" :key="i" /></div>
  <EmptyState v-else-if="!upcoming.length" icon="calendar" title="Aucune échéance à venir"
    text="Ajoutez une date d'échéance à vos tâches pour les voir ici." />
  <ul v-else class="list">
    <li v-for="t in upcoming" :key="t.id" class="card row" :class="{ overdue: isOverdue(t) }">
      <div class="grow">
        <strong>{{ t.titre }}</strong>
        <div class="chips">
          <span class="chip" :class="'prio-' + t.priorite"><AppIcon name="flag" :size="13" /> {{ priorityLabel(t.priorite) }}</span>
        </div>
      </div>
      <span class="chip" :class="isOverdue(t) ? 'chip-late' : 'chip-date'">
        <AppIcon name="calendar" :size="13" /> {{ dueLabel(t.dateEcheance) }}
      </span>
    </li>
  </ul>
</template>
