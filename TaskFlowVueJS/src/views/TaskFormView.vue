<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, ApiError } from '../services/api'
import { PRIORITIES } from '../utils/task'
import type { Priority, TaskPayload } from '../types'
import AppIcon from '../components/AppIcon.vue'

const props = defineProps<{ id?: string }>()
const router = useRouter()
const isEdit = computed(() => !!props.id)

const form = reactive<{ titre: string; description: string; priorite: Priority; dateEcheance: string }>({
  titre: '', description: '', priorite: 'NORMALE', dateEcheance: ''
})
const errors = ref<Record<string, string>>({})   
const globalError = ref('')
const saving = ref(false)
const loading = ref(false)

onMounted(async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const t = await api.task(props.id!)
    form.titre = t.titre
    form.description = t.description || ''
    form.priorite = t.priorite
    form.dateEcheance = t.dateEcheance ? t.dateEcheance.substring(0, 10) : ''
  } catch (e) {
    globalError.value = (e as Error).message
  } finally {
    loading.value = false
  }
})

/** Validation côté client (le backend valide aussi). */
function validate() {
  const e: Record<string, string> = {}
  if (!form.titre.trim()) e.titre = 'Le titre est obligatoire.'
  else if (form.titre.length > 100) e.titre = 'Le titre ne doit pas dépasser 100 caractères.'
  errors.value = e
  return Object.keys(e).length === 0
}

async function submit() {
  globalError.value = ''
  if (!validate()) return
  saving.value = true

  const payload: TaskPayload = {
    titre: form.titre.trim(),
    description: form.description || null,
    priorite: form.priorite,
    dateEcheance: form.dateEcheance || null
  }

  try {
    if (props.id) await api.updateTask(props.id, payload)
    else await api.createTask(payload)
    router.push('/tasks')
  } catch (e) {
    // Affiche les erreurs de l'API champ par champ
    if (e instanceof ApiError && Object.keys(e.fieldErrors).length) errors.value = e.fieldErrors
    globalError.value = (e as Error).message
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="page-head">
    <div>
      <h1>{{ isEdit ? 'Modifier la tâche' : 'Nouvelle tâche' }}</h1>
      <p class="subtitle">Les champs marqués d'une * sont obligatoires.</p>
    </div>
  </div>

  <form class="card form" novalidate @submit.prevent="submit">
    <p v-if="globalError" class="alert-error">{{ globalError }}</p>

    <div class="field">
      <label for="titre">Titre *</label>
      <input id="titre" v-model="form.titre" data-test="titre" maxlength="100" placeholder="Ex : Préparer la présentation"
        :class="{ invalid: errors.titre }" :disabled="loading" autofocus />
      <div class="field-foot">
        <small v-if="errors.titre" class="error" data-test="titre-error">{{ errors.titre }}</small>
        <small class="counter">{{ form.titre.length }}/100</small>
      </div>
    </div>

    <div class="field">
      <label for="description">Description</label>
      <textarea id="description" v-model="form.description" rows="4" placeholder="Détails, contexte… (facultatif)" :disabled="loading" />
      <small v-if="errors.description" class="error">{{ errors.description }}</small>
    </div>

    <div class="field">
      <label>Priorité</label>
      <div class="prio-picker">
        <button v-for="p in PRIORITIES" :key="p.value" type="button" class="prio-opt" :class="['prio-' + p.value, { active: form.priorite === p.value }]"
          @click="form.priorite = p.value">
          <AppIcon name="flag" :size="15" /> {{ p.label }}
        </button>
      </div>
      <small v-if="errors.priorite" class="error">{{ errors.priorite }}</small>
    </div>

    <div class="field">
      <label for="echeance">Date d'échéance</label>
      <input id="echeance" v-model="form.dateEcheance" type="date" :disabled="loading" />
      <small v-if="errors.dateEcheance" class="error">{{ errors.dateEcheance }}</small>
    </div>

    <div class="actions">
      <router-link to="/tasks" class="btn btn-outline">Annuler</router-link>
      <button class="btn" type="submit" :disabled="saving || loading">
        <AppIcon name="check" :size="18" /> {{ saving ? 'Enregistrement…' : 'Enregistrer' }}
      </button>
    </div>
  </form>
</template>
