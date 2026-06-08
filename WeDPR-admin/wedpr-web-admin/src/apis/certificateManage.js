import http from '../utils/http'

// 获取证书列表
const getCertList = (params) => http.get('/admin/getCertList', params)
// 获取证书详情
const getCertDetail = (params) => http.get('/admin/getCsrDetail/' + params.certId, params)
// 获取证书列表
const getNoCertAgencyList = (params) => http.get('/admin/getNoCertAgencyList/', params)
// 新增编辑证书
const createCert = (params) => http.post('/admin/createCert', params)
// 删除证书
const deleteCert = (params) => http.post('/admin/deleteCert/' + params.certId)
// 删除证书
const downloadCert = (params) => http.get('/admin/downloadCert/' + params.certId)
// 编辑证书
const setCert = (params) => http.post('/admin/setCert', params)
// 下载模板
const downloadCertTool = (params) => http.get('/admin/downloadCertTool', params)

export default {
  getCertList,
  createCert,
  deleteCert,
  setCert,
  downloadCert,
  getCertDetail,
  getNoCertAgencyList,
  downloadCertTool
}
