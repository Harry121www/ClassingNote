import { defineStore } from 'pinia'

export const useAnalysisStore = defineStore('analysis', {
  state: () => ({
    phase: 'idle',
    currentStage: '',
    currentTarget: '',
    streamContent: '',
    error: null,
    fileTree: [],
    projectName: '',
    analysisMode: 'ARCHITECTURE'
  }),
  actions: {
    reset() {
      this.phase = 'idle'
      this.currentStage = ''
      this.currentTarget = ''
      this.streamContent = ''
      this.error = null
      this.fileTree = []
      this.projectName = ''
    },
    resetAnalysis() {
      this.phase = 'analyzing'
      this.currentStage = ''
      this.currentTarget = ''
      this.streamContent = ''
      this.error = null
    },
    setFileTree(tree) {
      this.fileTree = tree
    },
    toggleFile(path) {
      const file = this.fileTree.find(f => f.path === path)
      if (file) file.selected = !file.selected
    },
    selectAll() {
      this.fileTree.forEach(f => { f.selected = true })
    },
    deselectAll() {
      this.fileTree.forEach(f => { f.selected = false })
    },
    getSelectedFiles() {
      return this.fileTree.filter(f => f.selected).map(f => ({ path: f.path, content: f.content }))
    },
    handleEvent(event) {
      if (event.phase === 'ERROR') {
        this.phase = 'error'
        this.error = event.content
        return
      }
      if (event.phase === 'DONE') {
        this.phase = 'done'
        return
      }
      if (this.phase !== 'analyzing') {
        this.phase = 'analyzing'
      }
      this.currentStage = event.phase
      this.currentTarget = event.path || ''
      this.streamContent += event.content
    },
    setError(msg) {
      this.phase = 'error'
      this.error = msg
    }
  }
})
