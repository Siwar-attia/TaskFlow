<script setup lang="ts">
import EmptyState from '../components/EmptyState.vue'
</script>

<template>
  <EmptyState icon="shield" title="Accès refusé" text="Vous n'avez pas les droits pour accéder à cette page.">
    <router-link to="/" class="btn">Retour à l'accueil</router-link>
  </EmptyState>
</template>
