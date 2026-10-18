import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const { tasksFn, toggleFn } = vi.hoisted(() => ({ tasksFn: vi.fn(), toggleFn: vi.fn() }))
vi.mock('../src/services/api', () => ({ api: { tasks: tasksFn, toggleTask: toggleFn, deleteTask: vi.fn() } }))

import TasksView from '../src/views/TasksView.vue'

const T = [
  { id: 1, titre: 'A faire', description: null, priorite: 'HAUTE', dateEcheance: null, termine: false },
  { id: 2, titre: 'Fini', description: null, priorite: 'BASSE', dateEcheance: null, termine: true }
]
const mountView = async () => {
  const w = mount(TasksView, { global: { stubs: { 'router-link': true } } })
  await flushPromises()
  return w
}

describe('TasksView', () => {
  beforeEach(() => { tasksFn.mockReset(); toggleFn.mockReset(); tasksFn.mockResolvedValue({ content: T, totalPages: 1 }) })

  it('affiche le statut "En cours" ou "Terminée" pour chaque tâche', async () => {
    const w = await mountView()
    const status = w.findAll('[data-test=status]').map((s) => s.text())
    expect(status).toEqual(['En cours', 'Terminée'])
  })

  it('affiche un bouton adapté : "Marquer terminée" ou "Rouvrir"', async () => {
    const w = await mountView()
    const buttons = w.findAll('[data-test=toggle]').map((b) => b.text())
    expect(buttons).toEqual(['Marquer terminée', 'Rouvrir'])
  })

  it('appelle l\'API de changement de statut au clic', async () => {
    toggleFn.mockResolvedValue({})
    const w = await mountView()
    await w.findAll('[data-test=toggle]')[0].trigger('click')
    expect(toggleFn).toHaveBeenCalledWith(1)
  })

  it('applique le filtre de statut même si l\'API l\'ignore', async () => {
    const w = await mountView()
    await w.findAll('.segmented button')[2].trigger('click') // onglet "Terminées"
    await flushPromises()
    expect(w.findAll('.task')).toHaveLength(1)
    expect(w.text()).toContain('Fini')
  })
})
