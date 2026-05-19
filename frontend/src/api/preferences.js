import api from './index'

export const preferenceApi = {
  get() { return api.get('/preferences') },
  update(data) { return api.put('/preferences', data) }
}
