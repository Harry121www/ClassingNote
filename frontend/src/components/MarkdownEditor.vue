<template>
  <div style="height:100%; overflow:hidden;">
    <textarea ref="editorRef" :value="modelValue" @input="onInput"
      class="md-editor"
      :style="{ fontSize: fontSize + 'px' }"
      placeholder="使用 Markdown 语法编写笔记..."></textarea>
  </div>
</template>

<script>
import { ref } from 'vue'

export default {
  name: 'MarkdownEditor',
  props: { modelValue: String, fontSize: { type: Number, default: 14 } },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const editorRef = ref(null)
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
      if (start === end) return // no selection
      const newVal = val.substring(0, start) + before + val.substring(start, end) + after + val.substring(end)
      emit('update:modelValue', newVal)
    }

    function insertAtCursor(text) {
      const el = editorRef.value
      if (!el) return
      const start = el.selectionStart
      const val = el.value
      const newVal = val.substring(0, start) + text + val.substring(start)
      emit('update:modelValue', newVal)
    }

    return { editorRef, onInput, getSelection, wrapSelection, insertAtCursor }
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
