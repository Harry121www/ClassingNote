import api from './index'

export const attachmentApi = {
  upload(formData) { return api.post('/attachments/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } }) },
  listByNote(noteId) { return api.get(`/notes/${noteId}/attachments`) },
  delete(id) { return api.delete(`/attachments/${id}`) }
}
