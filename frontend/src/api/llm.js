import api from './index'

export const llmApi = {
  getConfig: () => api.get('/llm/config'),
  updateConfig: (data) => api.put('/llm/config', data)
}
