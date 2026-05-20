<template>
  <div v-if="files.length > 0" class="file-tree">
    <div class="file-tree__header">
      <span>{{ files.length }} 个文件</span>
      <div>
        <el-button size="small" text @click="$emit('selectAll')">全选</el-button>
        <el-button size="small" text @click="$emit('deselectAll')">取消全选</el-button>
      </div>
    </div>
    <el-tree
      :data="treeData"
      :props="{ children: 'children', label: 'label' }"
      node-key="key"
      show-checkbox
      :default-expand-all="files.length < 50"
      :default-checked-keys="checkedKeys"
      @check-change="onCheckChange"
    >
      <template #default="{ data }">
        <span class="file-tree__node">
          <el-icon v-if="data.isDir"><Folder /></el-icon>
          <el-icon v-else><Document /></el-icon>
          <span>{{ data.label }}</span>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script>
import { computed } from 'vue'
import { Folder, Document } from '@element-plus/icons-vue'

export default {
  name: 'FileTreeSelector',
  components: { Folder, Document },
  props: {
    files: { type: Array, default: () => [] }
  },
  emits: ['toggleFile'],
  setup(props, { emit }) {
    function buildTree(fileList) {
      const root = {}
      for (const f of fileList) {
        const parts = f.path.replace(/\\/g, '/').split('/')
        let current = root
        for (let i = 0; i < parts.length; i++) {
          const part = parts[i]
          if (i === parts.length - 1) {
            if (!current._files) current._files = []
            current._files.push({ label: part, key: f.path, isDir: false })
          } else {
            if (!current[part]) current[part] = {}
            current = current[part]
          }
        }
      }
      return convertNode(root, '')
    }

    function convertNode(node, prefix) {
      const result = []
      for (const [name, child] of Object.entries(node)) {
        if (name.startsWith('_')) continue
        const fullPath = prefix ? prefix + '/' + name : name
        result.push({
          label: name,
          key: fullPath,
          isDir: true,
          children: convertNode(child, fullPath)
        })
      }
      if (node._files) {
        result.push(...node._files)
      }
      return result
    }

    const treeData = computed(() => buildTree(props.files))
    const checkedKeys = computed(() =>
      props.files.filter(f => f.selected).map(f => f.path)
    )

    function onCheckChange(data, checked) {
      if (!data.isDir) {
        emit('toggleFile', data.key)
      }
    }

    return { treeData, checkedKeys, onCheckChange }
  }
}
</script>

<style scoped>
.file-tree {
  margin-top: 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 12px;
  max-height: 400px;
  overflow-y: auto;
}
.file-tree__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
  font-size: 13px;
  color: var(--text-secondary);
}
.file-tree__node {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
</style>
