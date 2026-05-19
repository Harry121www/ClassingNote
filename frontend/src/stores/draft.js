import { defineStore } from 'pinia'
import { draftApi } from '../api/drafts'

export const useDraftStore = defineStore('draft', {
  state: () => ({
    draft: null
  }),
  actions: {
    async save(data) {
      const res = await draftApi.save(data)
      this.draft = res.data
    },
    async fetchLatest() {
      const res = await draftApi.getLatest()
      this.draft = res.data
    },
    async clear() {
      await draftApi.delete()
      this.draft = null
    }
  }
})
