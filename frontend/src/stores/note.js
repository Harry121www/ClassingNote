import { defineStore } from 'pinia'
import { noteApi } from '../api/notes'

export const useNoteStore = defineStore('note', {
  state: () => ({
    notes: [],
    currentNote: null,
    loading: false,
    total: 0
  }),
  actions: {
    async fetchNotes(params = {}) {
      this.loading = true
      try {
        const res = await noteApi.list(params)
        this.notes = res.data.content || []
        this.total = res.data.totalElements || 0
      } finally {
        this.loading = false
      }
    },
    async fetchNote(id) {
      const res = await noteApi.get(id)
      this.currentNote = res.data
      return this.currentNote
    },
    async create(data) {
      const res = await noteApi.create(data)
      return res.data
    },
    async update(id, data) {
      const res = await noteApi.update(id, data)
      this.currentNote = res.data
      return res.data
    },
    async remove(id) {
      await noteApi.delete(id)
      this.currentNote = null
    },
    async batchRemove(ids) {
      await noteApi.batchDelete(ids)
    }
  }
})
