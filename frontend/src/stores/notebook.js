import { defineStore } from 'pinia'
import { notebookApi } from '../api/notebooks'

export const useNotebookStore = defineStore('notebook', {
  state: () => ({
    notebooks: [],
    loading: false,
    total: 0
  }),
  actions: {
    async fetchNotebooks(params = {}) {
      this.loading = true
      try {
        const res = await notebookApi.list(params)
        this.notebooks = res.data.content || []
        this.total = res.data.totalElements || 0
      } finally {
        this.loading = false
      }
    },
    async create(data) {
      await notebookApi.create(data)
      await this.fetchNotebooks()
    },
    async update(id, data) {
      await notebookApi.update(id, data)
      await this.fetchNotebooks()
    },
    async remove(id) {
      await notebookApi.delete(id)
      await this.fetchNotebooks()
    }
  }
})
