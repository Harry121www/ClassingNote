import api from './index'

export const tagApi = {
  list(params) { return api.get('/tags', { params }) },
  create(data) { return api.post('/tags', data) },
  update(id, data) { return api.put(`/tags/${id}`, data) },
  delete(id) { return api.delete(`/tags/${id}`) }
}
