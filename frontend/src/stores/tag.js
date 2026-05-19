import { defineStore } from 'pinia'
import { tagApi } from '../api/tags'

export const useTagStore = defineStore('tag', {
  state: () => ({
    tags: [],
    loading: false
  }),
  actions: {
    async fetchTags(params = {}) {
      this.loading = true
      try {
        const res = await tagApi.list(params)
        this.tags = res.data || []
      } finally {
        this.loading = false
      }
    },
    async create(data) {
      await tagApi.create(data)
      await this.fetchTags()
    },
    async update(id, data) {
      await tagApi.update(id, data)
      await this.fetchTags()
    },
    async remove(id) {
      await tagApi.delete(id)
      await this.fetchTags()
    }
  }
})
