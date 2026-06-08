import http from '../utils/http'

// 查询明细
const queryRecordSyncStatus = (params) => http.get('/admin/queryRecordSyncStatus', params)

export default { queryRecordSyncStatus }
