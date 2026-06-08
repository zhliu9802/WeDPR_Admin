import http from '../utils/http'

// 查询数据集大屏明细
const getDatasetInfo = (params) => http.get('/admin/dashboard/dataset', params)
// 查询数据集折线图
const getDatasetLineData = (params) => http.get('/admin/dashboard/dataset-dateline', params)
// 查询任务明细
const getJobInfo = (params) => http.get('/admin/dashboard/job', params)
// 查询任务趋势图
const getJobLineData = (params) => http.get('/admin/dashboard/job-dateline', params)
// 查询机构节点图
const getAgencyInfo = (params) => http.get('/admin/dashboard/agency', params)

export default { getDatasetInfo, getDatasetLineData, getJobInfo, getJobLineData, getAgencyInfo }
