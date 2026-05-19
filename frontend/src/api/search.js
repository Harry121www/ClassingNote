import api from './index'

export const searchApi = {
  search(params) { return api.get('/search', { params }) }
}
