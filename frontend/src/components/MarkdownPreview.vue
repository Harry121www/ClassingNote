<template>
  <div style="height:100%; overflow-y:auto; padding:16px;"
    v-html="rendered" class="markdown-body"></div>
</template>

<script>
import { computed } from 'vue'
import { marked } from 'marked'

marked.setOptions({ breaks: true, gfm: true })

export default {
  name: 'MarkdownPreview',
  props: { content: String },
  setup(props) {
    const rendered = computed(() => marked(props.content || ''))
    return { rendered }
  }
}
</script>

<style>
.markdown-body { line-height:1.8; }
.markdown-body h1 { font-size:1.8em; border-bottom:2px solid var(--brand-color); padding-bottom:8px; margin:16px 0; }
.markdown-body h2 { font-size:1.4em; margin:14px 0; }
.markdown-body h3 { font-size:1.2em; margin:12px 0; }
.markdown-body pre { background:var(--bg-code); padding:12px; border-radius:6px; overflow-x:auto; }
.markdown-body code { background:var(--bg-code-inline); padding:2px 6px; border-radius:3px; font-size:0.9em; }
.markdown-body pre code { background:none; padding:0; }
.markdown-body img { max-width:100%; border-radius:4px; }
.markdown-body blockquote { border-left:4px solid var(--brand-color); padding-left:16px; color:var(--text-muted); margin:12px 0; }
.markdown-body table { border-collapse:collapse; width:100%; }
.markdown-body th, .markdown-body td { border:1px solid var(--table-border); padding:8px 12px; }
.markdown-body th { background:var(--bg-code); }
.markdown-body em { background:var(--bg-highlight); padding:0 2px; font-style:normal; }
</style>
