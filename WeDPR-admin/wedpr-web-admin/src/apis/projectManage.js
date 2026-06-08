import http from '../utils/http'

// 管理员查询列表
const adminQueryProject = (params) => http.get('/admin/listProject', params)
// 管理员查询项目内任务列表
const adminQuerylistJobInProject = (params) => http.get('/admin/listJob', params)
// 管理员查询项目内任务列表
const adminQueryJobsByDatasetId = (params) => http.get('/admin/queryJobsByDatasetId', params)

export default {
  adminQuerylistJobInProject,
  adminQueryProject,
  adminQueryJobsByDatasetId
}
