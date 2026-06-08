import http from '../utils/http'

// 编辑用户密码
const modifyPassword = (params) => http.patch('/users-password', params)
// 查询机构下用户
const getUser = (params) => http.get('/users', params)

export default {
  modifyPassword,
  getUser
}
