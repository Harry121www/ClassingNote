import { defineStore } from 'pinia'
import { preferenceApi } from '../api/preferences'

export const usePreferenceStore = defineStore('preference', {
  state: () => ({
    theme: 'light',
    autoSaveInterval: 30,
    editorFontSize: 14
  }),
  actions: {
    async fetch() {
      try {
        const res = await preferenceApi.get()
        if (res.data) {
          this.theme = res.data.theme || 'light'
          this.autoSaveInterval = res.data.autoSaveInterval || 30
          this.editorFontSize = res.data.editorFontSize || 14
        }
      } catch (e) { /* use defaults */ }
    },
    async update(data) {
      const res = await preferenceApi.update(data)
      if (res.data) {
        this.theme = res.data.theme
        this.autoSaveInterval = res.data.autoSaveInterval
        this.editorFontSize = res.data.editorFontSize
      }
    },
    toggleTheme() {
      this.theme = this.theme === 'dark' ? 'light' : 'dark'
      document.documentElement.classList.toggle('dark', this.theme === 'dark')
      preferenceApi.update({ theme: this.theme }).catch(() => {})
    },
    applyTheme() {
      document.documentElement.classList.toggle('dark', this.theme === 'dark')
    }
  }
})
