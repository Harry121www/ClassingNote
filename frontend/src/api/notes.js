import api from './index'

export const noteApi = {
  list(params) { return api.get('/notes', { params }) },
  get(id) { return api.get(`/notes/${id}`) },
  create(data) { return api.post('/notes', data) },
  update(id, data) { return api.put(`/notes/${id}`, data) },
  delete(id) { return api.delete(`/notes/${id}`) },
  batchDelete(ids) { return api.delete('/notes/batch', { data: { ids } }) }
}
