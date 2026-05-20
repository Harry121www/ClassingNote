import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../components/AppLayout.vue'),
    redirect: '/notes',
    children: [
      {
        path: 'notes',
        name: 'NoteList',
        component: () => import('../views/NotebookView.vue')
      },
      {
        path: 'notes/:id/edit',
        name: 'NoteEditor',
        component: () => import('../views/NoteEditor.vue'),
        props: true
      },
      {
        path: 'notes/new',
        name: 'NewNote',
        component: () => import('../views/NoteEditor.vue')
      },
      {
        path: 'tags',
        name: 'TagManager',
        component: () => import('../views/TagManager.vue')
      },
      {
        path: 'search',
        name: 'SearchView',
        component: () => import('../views/SearchView.vue')
      },
      {
        path: 'analyzer',
        name: 'ProjectAnalyzer',
        component: () => import('../views/ProjectAnalyzer.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/SettingsView.vue')
      },
      {
        path: 'knowledge',
        name: 'KnowledgeBase',
        component: () => import('../views/KnowledgeBase.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
