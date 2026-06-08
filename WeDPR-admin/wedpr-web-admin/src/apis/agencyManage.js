import http from '../utils/http'

// 获取机构列表
const getAgencyList = (params) => http.get('/admin/getAgencyList', params)
// 新增机构
const createAgency = (params) => http.post('/admin/createAgency', params)
// 删除机构
const deleteAgency = (params) => http.post('/admin/deleteAgency/' + params.agencyId)
// 编辑机构
const updateAgency = (params) => http.post('/admin/updateAgency', params)
// 编辑机构
const getAgencyDetail = (params) => http.get('/admin/getAgencyDetail/' + params.agencyId, params)
// 启用、禁用机构
const setAgency = (params) => http.post('/admin/setAgency', params)

export default {
  getAgencyList,
  createAgency,
  deleteAgency,
  updateAgency,
  setAgency,
  getAgencyDetail
}
