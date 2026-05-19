import api from './index'

export const exportApi = {
  exportNote(id, targetDir) { return api.get(`/export/notes/${id}`, { params: { targetDir } }) },
  exportNotebook(id, targetDir) { return api.get(`/export/notebooks/${id}`, { params: { targetDir } }) }
}
