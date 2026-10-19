<script setup lang="ts">
import { computed, reactive, ref, watch, onMounted } from 'vue'
import { api } from '../services/api'
import { dueLabel, isOverdue, priorityLabel, PRIORITIES } from '../utils/task'
import type { Task } from '../types'
import AppPagination from '../components/AppPagination.vue'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import EmptyState from '../components/EmptyState.vue'
import AppIcon from '../components/AppIcon.vue'

const tasks = ref<Task[]>([])
const totalPages = ref(0)
const error = ref('')
const loading = ref(true)
const busyId = ref<Task['id'] | null>(null) // évite les doubles clics
const toDelete = ref<Task | null>(null)     // tâche en attente de confirmation

const filters = reactive({
  page: 0,
  termine: '',   // '' = toutes, 'false' = en cours, 'true' = terminées
  priorite: '',
  sort: 'dateEcheance,asc'
})

const STATUS_TABS = [
  { value: '', label: 'Toutes' },
  { value: 'false', label: 'En cours' },
  { value: 'true', label: 'Terminées' }
]

const hasFilters = computed(() => filters.termine !== '' || filters.priorite !== '')

async function load() {
  error.value = ''
  loading.value = true

  try {
    const [sortBy, direction] = filters.sort.split(',')

    const res = await api.tasks({
      page: filters.page,
      size: 8,
      completed: filters.termine === '' ? undefined : filters.termine === 'true',
      priority: filters.priorite || undefined,
      sortBy,
      direction
    })

    tasks.value = res.content
    totalPages.value = res.totalPages
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

// Quand un filtre change on revient à la page 0
watch(() => [filters.termine, filters.priorite, filters.sort], () => {
  filters.page = 0
  load()
})
onMounted(load)

function goTo(p: number) {
  filters.page = p
  load()
}

function resetFilters() {
  filters.termine = ''
  filters.priorite = ''
}

async function toggle(task: Task) {
  if (busyId.value !== null) return
  busyId.value = task.id
  try {
    await api.toggleTask(task.id)
    await load()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    busyId.value = null
  }
}

async function confirmDelete() {
  const task = toDelete.value!
  toDelete.value = null
  try {
    await api.deleteTask(task.id)
    // si on supprime le dernier élément d'une page, on recule d'une page
    if (tasks.value.length === 1 && filters.page > 0) filters.page--
    await load()
  } catch (e) {
    error.value = (e as Error).message
  }
}
</script>

<template>
  <div class="page-head">
    <div>
      <h1>Mes tâches</h1>
      <p class="subtitle">Gérez, filtrez et terminez vos tâches.</p>
    </div>
    <router-link to="/tasks/new" class="btn"><AppIcon name="plus" :size="18" /> Nouvelle tâche</router-link>
  </div>

  <div class="toolbar">
    <div class="segmented" role="tablist" aria-label="Statut">
      <button v-for="s in STATUS_TABS" :key="s.value" role="tab" :aria-selected="filters.termine === s.value"
        :class="{ active: filters.termine === s.value }" @click="filters.termine = s.value">
        {{ s.label }}
      </button>
    </div>
    <select v-model="filters.priorite" aria-label="Priorité">
      <option value="">Toutes priorités</option>
      <option v-for="p in PRIORITIES" :key="p.value" :value="p.value">{{ p.label }}</option>
    </select>
    <select v-model="filters.sort" aria-label="Tri">
      <option value="dateEcheance,asc">Échéance : proche d'abord</option>
      <option value="dateEcheance,desc">Échéance : lointaine d'abord</option>
      <option value="dateCreation,desc">Création : récentes d'abord</option>
      <option value="dateCreation,asc">Création : anciennes d'abord</option>
    </select>
    <button v-if="hasFilters" class="btn btn-ghost" @click="resetFilters">Réinitialiser</button>
  </div>

  <p v-if="error" class="alert-error">{{ error }}</p>

  <div v-if="loading" class="skeleton-list"><div class="skeleton" v-for="i in 4" :key="i" /></div>

  <EmptyState v-else-if="!tasks.length && !error" icon="inbox"
    :title="hasFilters ? 'Aucun résultat' : 'Aucune tâche pour le moment'"
    :text="hasFilters ? 'Essayez de modifier ou réinitialiser les filtres.' : 'Créez votre première tâche pour démarrer.'">
    <router-link v-if="!hasFilters" to="/tasks/new" class="btn"><AppIcon name="plus" :size="18" /> Créer une tâche</router-link>
  </EmptyState>

  <ul v-else class="list">
    <li v-for="t in tasks" :key="t.id" class="card task" :class="{ overdue: isOverdue(t), done: t.termine }">
      <div class="grow">
        <div class="task-title-row">
          <span class="task-title">{{ t.titre }}</span>
          <span class="chip" :class="t.termine ? 'chip-done' : 'chip-open'" data-test="status">
            <AppIcon :name="t.termine ? 'checkCircle' : 'clock'" :size="13" /> {{ t.termine ? 'Terminée' : 'En cours' }}
          </span>
        </div>
        <p v-if="t.description" class="task-desc">{{ t.description }}</p>
        <div class="chips">
          <span class="chip" :class="'prio-' + t.priorite"><AppIcon name="flag" :size="13" /> {{ priorityLabel(t.priorite) }}</span>
          <span class="chip" :class="isOverdue(t) ? 'chip-late' : 'chip-date'">
            <AppIcon :name="isOverdue(t) ? 'alert' : 'calendar'" :size="13" /> {{ dueLabel(t.dateEcheance) }}
          </span>
        </div>
      </div>

      <div class="task-actions">
        <button class="btn btn-sm" :class="t.termine ? 'btn-outline' : 'btn-done'" :disabled="busyId === t.id"
          data-test="toggle" @click="toggle(t)">
          <AppIcon :name="t.termine ? 'undo' : 'check'" :size="15" />
          {{ t.termine ? 'Rouvrir' : 'Terminer' }}
        </button>
        <router-link :to="`/tasks/${t.id}/edit`" class="icon-btn" title="Modifier" aria-label="Modifier">
          <AppIcon name="pencil" :size="18" />
        </router-link>
        <button class="icon-btn danger" title="Supprimer" aria-label="Supprimer" @click="toDelete = t">
          <AppIcon name="trash" :size="18" />
        </button>
      </div>
    </li>
  </ul>

  <AppPagination :page="filters.page" :total-pages="totalPages" @change="goTo" />

  <ConfirmDialog v-if="toDelete" :message="`Supprimer la tâche « ${toDelete.titre} » ? Cette action est définitive.`"
    @confirm="confirmDelete" @cancel="toDelete = null" />
</template>
