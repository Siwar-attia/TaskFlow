import type { Priority, Stats, Task } from '../types'

export const PRIORITIES: { value: Priority; label: string }[] = [
  { value: 'BASSE', label: 'Basse' },
  { value: 'NORMALE', label: 'Normale' },
  { value: 'HAUTE', label: 'Haute' }
]

export const priorityLabel = (v: string): string => PRIORITIES.find((p) => p.value === v)?.label || v

/** Date du jour au format YYYY-MM-DD, en heure locale. */
function todayLocal(): string {
  const d = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/**
 * Une tâche est en retard si elle n'est pas terminée et que son échéance
 * (YYYY-MM-DD) est strictement avant aujourd'hui.
 * On compare des chaînes ISO : pas de problème de fuseau horaire.
 */
export function isOverdue(task: Pick<Task, 'termine' | 'dateEcheance'>): boolean {
  if (task.termine || !task.dateEcheance) return false
  return task.dateEcheance.substring(0, 10) < todayLocal()
}

export function formatDate(d: string | null | undefined): string {
  if (!d) return '—'
  const [y, m, day] = d.substring(0, 10).split('-')
  return `${day}/${m}/${y}`
}

/** Nombre de jours entre aujourd'hui et une date (négatif = passé). */
export function daysUntil(d: string): number {
  const [y, m, day] = d.substring(0, 10).split('-').map(Number)
  const [ty, tm, td] = todayLocal().split('-').map(Number)
  return Math.round((Date.UTC(y, m - 1, day) - Date.UTC(ty, tm - 1, td)) / 86400000)
}

/** Texte lisible pour une échéance : "Aujourd'hui", "Dans 3 jours", "En retard de 2 jours"… */
export function dueLabel(d: string | null | undefined): string {
  if (!d) return 'Sans échéance'
  const n = daysUntil(d)
  if (n === 0) return "Aujourd'hui"
  if (n === 1) return 'Demain'
  if (n === -1) return 'Hier'
  if (n > 1 && n <= 14) return `Dans ${n} jours`
  if (n < -1 && n >= -14) return `En retard de ${-n} jours`
  return formatDate(d)
}

/** Initiales pour l'avatar : "alice Alice" -> "AA". */
export function initials(name: string | undefined | null): string {
  const parts = (name || '?').trim().split(/\s+/).filter(Boolean)
  return ((parts[0]?.[0] || '?') + (parts.length > 1 ? parts[parts.length - 1][0] : '')).toUpperCase()
}

/** Normalise les stats : accepte des noms anglais ou français renvoyés par l'API. */
export function normalizeStats(s: Record<string, any> = {}): Stats {
  return {
    completed: s.completed ?? s.done ?? s.terminees ?? s.terminee ?? s.termine ?? s.nbTerminees ?? s.nombreTerminees ?? 0,
    inProgress: s.inProgress ?? s.pending ?? s.active ?? s.enCours ?? s.nbEnCours ?? s.nombreEnCours ?? 0,
    overdue: s.overdue ?? s.late ?? s.enRetard ?? s.retard ?? s.nbEnRetard ?? s.nombreEnRetard ?? 0
  }
}
