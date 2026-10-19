import { describe, it, expect } from 'vitest'
import { isOverdue } from '../src/utils/task'

describe('isOverdue', () => {
  it('vrai si échéance passée et non terminée', () => {
    expect(isOverdue({ termine: false, dateEcheance: '2022-02-02' })).toBe(true)
  })
  it('faux si terminée', () => {
    expect(isOverdue({ termine: true, dateEcheance: '2022-02-02' })).toBe(false)
  })
  it('faux sans échéance', () => {
    expect(isOverdue({ termine: false, dateEcheance: null })).toBe(false)
  })
})
