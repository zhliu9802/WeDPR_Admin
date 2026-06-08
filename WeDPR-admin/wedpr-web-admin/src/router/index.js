import Vue from 'vue'
import VueRouter from 'vue-router'
Vue.use(VueRouter)
const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import(/* webpackChunkName: "login" */ '../views/adminLogin/index.vue'),
    meta: {
      title: '用户登录',
      requireAuth: false,
      permissionCheck: false,
      isParent: true
    }
  },
  {
    path: '/noPermission',
    component: () => import(/* webpackChunkName: "about" */ '../views/noPermission/index.vue'),
    meta: {
      title: '暂无权限',
      requireAuth: false,
      permissionCheck: false,
      isParent: true
    }
  },
  {
    path: '/',
    redirect: '/login',
    meta: {
      title: '',
      requireAuth: false,
      permissionCheck: false,
      isParent: true
    }
  },
  {
    name: 'screen',
    path: '/screen',
    component: () => import(/* webpackChunkName: "layout" */ '@/views/screen/index.vue'),
    meta: {
      title: '数据总览',
      isParent: true,
      requireAuth: true,
      permissionCheck: true,
      permissionNeed: ['screen']
    }
  },
  {
    path: '/layout',
    name: 'layout',
    redirect: '/tenantManage',
    component: () => import(/* webpackChunkName: "about" */ '../views/layout/index.vue'),
    children: [
      {
        name: 'dataManage',
        path: '/dataManage',
        component: () => import(/* webpackChunkName: "dataManage" */ '../views/adminDataManage/index.vue'),
        meta: {
          title: '数据资源',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['dataManage']
        }
      },
      {
        name: 'dataDetail',
        path: '/dataDetail',
        component: () => import(/* webpackChunkName: "dataManage" */ '../views/adminDataDetail/index.vue'),
        meta: {
          title: '数据详情',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['dataManage']
        }
      },
      {
        name: 'projectManage',
        path: '/projectManage',
        component: () => import(/* webpackChunkName: "projectManage" */ '../views/adminProjectManage/index.vue'),
        meta: {
          title: '项目空间',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['projectManage']
        }
      },
      {
        name: 'projectDetail',
        path: '/projectDetail',
        component: () => import(/* webpackChunkName: "projectDetail" */ '../views/adminProjectDetail/index.vue'),
        meta: {
          title: '项目详情',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['projectManage']
        }
      },
      {
        name: 'logManage',
        path: '/logManage',
        component: () => import(/* webpackChunkName: "logManage" */ '../views/adminLogManage/index.vue'),
        meta: {
          title: '日志审计',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['logManage']
        }
      },
      {
        name: 'certificateManage',
        path: '/certificateManage',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/certificateManage/index.vue'),
        meta: {
          title: '证书管理',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['certificateManage']
        }
      },
      {
        name: 'addCertificate',
        path: '/addCertificate',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/addCertificate/index.vue'),
        meta: {
          title: '新增证书',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['addCertificate']
        }
      },
      {
        name: 'agencyManage',
        path: '/agencyManage',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/agencyManage/index.vue'),
        meta: {
          title: '机构管理',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['agencyManage']
        }
      },
      {
        name: 'agencyCreate',
        path: '/agencyCreate',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/agencyCreate/index.vue'),
        meta: {
          title: '新增机构',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['agencyCreate']
        }
      }
    ]
  }
]

const router = new VueRouter({
  routes
})

// Vue Router 3.x：忽略重复导航/导航被取消，避免 Uncaught runtime errors 遮罩
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location, onResolve, onReject) {
  if (onResolve || onReject) {
    return originalPush.call(this, location, onResolve, onReject)
  }
  return originalPush.call(this, location).catch((err) => {
    if (err.name === 'NavigationDuplicated' || err.message.includes('Navigation cancelled')) {
      return err
    }
    return Promise.reject(err)
  })
}

const originalReplace = VueRouter.prototype.replace
VueRouter.prototype.replace = function replace(location, onResolve, onReject) {
  if (onResolve || onReject) {
    return originalReplace.call(this, location, onResolve, onReject)
  }
  return originalReplace.call(this, location).catch((err) => {
    if (err.name === 'NavigationDuplicated' || err.message.includes('Navigation cancelled')) {
      return err
    }
    return Promise.reject(err)
  })
}

export default router
