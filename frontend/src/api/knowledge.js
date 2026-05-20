import api from './index'

export const knowledgeApi = {
  create: (data) => api.post('/knowledge', data),
  update: (id, data) => api.put(`/knowledge/${id}`, data),
  listByProject: (project) => api.get('/knowledge', { params: { project } }),
  get: (id) => api.get(`/knowledge/${id}`),
  delete: (id) => api.delete(`/knowledge/${id}`)
}
