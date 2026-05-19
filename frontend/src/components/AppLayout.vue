<template>
  <el-container style="height:100%">
    <el-aside width="260px" style="border-right:1px solid #e4e7ed; display:flex; flex-direction:column;">
      <Sidebar />
    </el-aside>
    <el-main style="padding:0; overflow:hidden;">
      <router-view />
    </el-main>
  </el-container>
</template>

<script>
import Sidebar from './Sidebar.vue'
export default {
  name: 'AppLayout',
  components: { Sidebar }
}
</script>
