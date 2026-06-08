import http from '../utils/http'

const adminGetConfig = (params) => http.get('/admin/getConfig', params)
// 获取公钥
const getPub = (params) => http.get('/pub', params)
const getImageCode = (params) => http.get('/image-code', params)
export default { adminGetConfig, getPub, getImageCode }
