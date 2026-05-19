import api from './index'

export const draftApi = {
  save(data) { return api.post('/drafts', data) },
  getLatest() { return api.get('/drafts/latest') },
  delete() { return api.delete('/drafts') }
}
