import api from './index'

export const notebookApi = {
  list(params) { return api.get('/notebooks', { params }) },
  get(id) { return api.get(`/notebooks/${id}`) },
  create(data) { return api.post('/notebooks', data) },
  update(id, data) { return api.put(`/notebooks/${id}`, data) },
  delete(id) { return api.delete(`/notebooks/${id}`) }
}
