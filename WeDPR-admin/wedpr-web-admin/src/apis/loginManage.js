import http from '../utils/http'

// 登录
const adminLogin = (params) => http.post('/admin/login', params)

// 快捷登录

export default { adminLogin }
