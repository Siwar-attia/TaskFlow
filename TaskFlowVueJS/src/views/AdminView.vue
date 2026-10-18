<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '../services/api'
import { dueLabel, initials, normalizeStats } from '../utils/task'
import type { AdminStat, Task } from '../types'
import AppPagination from '../components/AppPagination.vue'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import EmptyState from '../components/EmptyState.vue'
import AppIcon from '../components/AppIcon.vue'

const tasks = ref<Task[]>([])
const stats = ref<AdminStat[]>([])
const page = ref(0)
const totalPages = ref(0)
const owner = ref('')
const error = ref('')
const loading = ref(true)
const toDelete = ref<Task | null>(null)

async function loadTasks() {
  try {
    const res = await api.adminTasks({ page: page.value, size: 10, owner: owner.value })
    tasks.value = res.content
    totalPages.value = res.totalPages
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    const data = await api.adminStats()
    // accepte un tableau [{owner, completed, ...}] ou un objet { alice: {...}, bob: {...} }
    stats.value = Array.isArray(data)
      ? data.map((s: Record<string, any>) => ({
          owner: s.owner ?? s.username ?? s.proprietaireNom ?? s.proprietaire ?? s.nom ?? '?',
          ...normalizeStats(s)
        }))
      : Object.entries(data as Record<string, Record<string, any>>).map(([name, s]) => ({ owner: name, ...normalizeStats(s) }))
  } catch (e) {
    error.value = (e as Error).message
  }
}

function filter() { page.value = 0; loadTasks() }
function clearFilter() { owner.value = ''; filter() }
function goTo(p: number) { page.value = p; loadTasks() }

async function confirmDelete() {
  const task = toDelete.value!
  toDelete.value = null
  try {
    await api.adminDelete(task.id)
    if (tasks.value.length === 1 && page.value > 0) page.value--
    await Promise.all([loadTasks(), loadStats()])
  } catch (e) {
    error.value = (e as Error).message
  }
}

onMounted(() => { loadTasks(); loadStats() })
</script>

<template>
  <div class="page-head">
    <div>
      <h1>Administration</h1>
      <p class="subtitle">Toutes les tâches et les statistiques par utilisateur.</p>
    </div>
  </div>

  <p v-if="error" class="alert-error">{{ error }}</p>

  <div class="section-head"><h2><AppIcon name="users" :size="20" /> Statistiques par utilisateur</h2></div>
  <div class="table-wrap">
    <table>
      <thead><tr><th>Utilisateur</th><th>Terminées</th><th>En cours</th><th>En retard</th></tr></thead>
      <tbody>
        <tr v-for="s in stats" :key="s.owner">
          <td><span class="who"><span class="avatar avatar-sm">{{ initials(s.owner) }}</span>{{ s.owner }}</span></td>
          <td><span class="pill pill-green">{{ s.completed }}</span></td>
          <td><span class="pill pill-violet">{{ s.inProgress }}</span></td>
          <td><span class="pill pill-red">{{ s.overdue }}</span></td>
        </tr>
        <tr v-if="!stats.length"><td colspan="4" class="muted center">Aucune statistique.</td></tr>
      </tbody>
    </table>
  </div>

  <div class="section-head"><h2><AppIcon name="tasks" :size="20" /> Toutes les tâches</h2></div>
  <form class="toolbar" @submit.prevent="filter">
    <input v-model="owner" placeholder="Filtrer par utilisateur (ex : alice)" aria-label="Propriétaire" />
    <button class="btn">Filtrer</button>
    <button v-if="owner" type="button" class="btn btn-ghost" @click="clearFilter">Effacer</button>
  </form>

  <div v-if="loading" class="skeleton-list"><div class="skeleton" v-for="i in 4" :key="i" /></div>
  <EmptyState v-else-if="!tasks.length" icon="inbox" title="Aucune tâche trouvée" />
  <div v-else class="table-wrap">
    <table>
      <thead><tr><th>Titre</th><th>Propriétaire</th><th>Échéance</th><th>Statut</th><th></th></tr></thead>
      <tbody>
        <tr v-for="t in tasks" :key="t.id">
          <td class="strong">{{ t.titre }}</td>
          <td><span class="who"><span class="avatar avatar-sm">{{ initials(t.proprietaireNom) }}</span>{{ t.proprietaireNom }}</span></td>
          <td>{{ dueLabel(t.dateEcheance) }}</td>
          <td><span class="pill" :class="t.termine ? 'pill-green' : 'pill-violet'">{{ t.termine ? 'Terminée' : 'En cours' }}</span></td>
          <td class="right">
            <button class="icon-btn danger" title="Supprimer" aria-label="Supprimer" @click="toDelete = t">
              <AppIcon name="trash" :size="18" />
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <AppPagination :page="page" :total-pages="totalPages" @change="goTo" />

  <ConfirmDialog v-if="toDelete" :message="`Supprimer la tâche « ${toDelete.titre} » de ${toDelete.proprietaireNom} ?`"
    @confirm="confirmDelete" @cancel="toDelete = null" />
</template>