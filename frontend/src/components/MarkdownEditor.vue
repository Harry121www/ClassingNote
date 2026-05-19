<template>
  <div style="height:100%; overflow:hidden;">
    <textarea ref="editorRef" :value="modelValue" @input="onInput"
      style="width:100%; height:100%; border:none; padding:16px; resize:none; outline:none;
             font-family:'Fira Code', monospace; line-height:1.6;"
      :style="{ fontSize: fontSize + 'px' }"
      placeholder="使用 Markdown 语法编写笔记..."></textarea>
  </div>
</template>

<script>
export default {
  name: 'MarkdownEditor',
  props: { modelValue: String, fontSize: { type: Number, default: 14 } },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const onInput = (e) => emit('update:modelValue', e.target.value)
    return { onInput }
  }
}
</script>
