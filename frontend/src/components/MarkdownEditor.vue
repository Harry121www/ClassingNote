<template>
  <div style="height:100%; overflow:hidden;">
    <textarea ref="editorRef" :value="modelValue" @input="onInput" @keydown="onKeydown"
      class="md-editor"
      :style="{ fontSize: fontSize + 'px' }"
      placeholder="使用 Markdown 语法编写笔记..."></textarea>
  </div>
</template>

<script>
import { ref, watch, nextTick } from 'vue'

export default {
  name: 'MarkdownEditor',
  props: { modelValue: String, fontSize: { type: Number, default: 14 } },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const editorRef = ref(null)
    const undoStack = ref([])
    const redoStack = ref([])
    let undoing = false
    let mergeUntil = 0
    let programmaticChange = false

    const onInput = (e) => emit('update:modelValue', e.target.value)

    function getSelection() {
      const el = editorRef.value
      if (!el) return null
      return { start: el.selectionStart, end: el.selectionEnd, text: el.value.substring(el.selectionStart, el.selectionEnd) }
    }

    function wrapSelection(before, after) {
      const el = editorRef.value
      if (!el) return
      const start = el.selectionStart
      const end = el.selectionEnd
      const val = el.value
      if (start === end) return
      programmaticChange = true
      const newVal = val.substring(0, start) + before + val.substring(start, end) + after + val.substring(end)
      emit('update:modelValue', newVal)
    }

    function insertAtCursor(text) {
      const el = editorRef.value
      if (!el) return
      const start = el.selectionStart
      const val = el.value
      programmaticChange = true
      const newVal = val.substring(0, start) + text + val.substring(start)
      emit('update:modelValue', newVal)
    }

    function undo() {
      if (undoStack.value.length === 0) return
      undoing = true
      mergeUntil = 0
      redoStack.value.push(props.modelValue)
      emit('update:modelValue', undoStack.value.pop())
      nextTick(() => { undoing = false })
    }

    function redo() {
      if (redoStack.value.length === 0) return
      undoing = true
      mergeUntil = 0
      undoStack.value.push(props.modelValue)
      emit('update:modelValue', redoStack.value.pop())
      nextTick(() => { undoing = false })
    }

    function onKeydown(e) {
      if (e.ctrlKey && !e.shiftKey && e.key === 'z') {
        e.preventDefault()
        undo()
      } else if (e.ctrlKey && e.shiftKey && e.key === 'Z') {
        e.preventDefault()
        redo()
      }
    }

    watch(() => props.modelValue, (newVal, oldVal) => {
      if (undoing || oldVal === undefined) return
      const now = Date.now()
      if (!programmaticChange && now < mergeUntil && undoStack.value.length > 0) return
      programmaticChange = false
      undoStack.value.push(oldVal)
      redoStack.value = []
      mergeUntil = now + 500
    })

    return { editorRef, onInput, onKeydown, getSelection, wrapSelection, insertAtCursor, undo, redo }
  }
}
</script>

<style>
.md-editor {
  width: 100%; height: 100%; border: none; padding: 16px;
  resize: none; outline: none;
  font-family: 'Fira Code', monospace; line-height: 1.6;
  background: var(--bg-page);
  color: var(--text-primary);
}
</style>
