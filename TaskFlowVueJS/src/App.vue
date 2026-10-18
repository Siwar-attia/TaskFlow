<script setup lang="ts">
import { ref } from 'vue'
import SideBar from './components/SideBar.vue'
import TopBar from './components/TopBar.vue'

const menuOpen = ref(false) // tiroir du menu sur mobile
</script>

<template>
  <div class="app-shell">
    <SideBar :open="menuOpen" @close="menuOpen = false" />
    <main class="main">
      <TopBar @menu="menuOpen = true" />
      <div class="container">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>
