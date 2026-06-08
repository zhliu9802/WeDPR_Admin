<template>
  <div class="cer-manage">
    <div class="form-search">
      <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
        <el-form-item>
          <el-button type="primary" icon="el-icon-plus" @click="newCertificate"> 新增证书 </el-button>
        </el-form-item>
        <div style="float: right">
          <el-form-item prop="agencyName" label="机构名称：">
            <el-select clearable size="small" style="width: 160px" v-model="searchForm.agencyName" placeholder="请选择">
              <el-option :key="item" v-for="item in agencyList" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item prop="createTime" label="签发时间：">
            <el-date-picker
              clearable
              style="width: 344px"
              type="datetimerange"
              value-format="yyyy-MM-dd HH:mm:ss"
              v-model="searchForm.createTime"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            />
          </el-form-item>
          <el-form-item prop="certStatus" label="证书状态：">
            <el-select clearable size="small" style="width: 160px" v-model="searchForm.certStatus" placeholder="请选择">
              <el-option label="有效" :value="certStatusEnum.OPEND"></el-option>
              <el-option label="过期" :value="certStatusEnum.OUTDATE"></el-option>
              <el-option label="禁用" :value="certStatusEnum.CLOSED"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="queryFlag" @click="queryHandle">
              {{ queryFlag ? '查询中...' : '查询' }}
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="default" :loading="queryFlag" @click="reset"> 重置 </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
    <div class="tableContent autoTableWrap" v-if="total">
      <el-table :max-height="tableHeight" size="small" v-loading="loadingFlag" :data="tableData" :border="true" class="table-wrap">
        <el-table-column label="证书编号" prop="certId" show-overflow-tooltip />
        <el-table-column label="绑定机构名称" prop="agencyName" show-overflow-tooltip />
        <el-table-column label="绑定机构编号" prop="agencyName" show-overflow-tooltip />
        <el-table-column label="签发时间" prop="signTime" show-overflow-tooltip />
        <el-table-column label="有效期至" prop="expireTime" show-overflow-tooltip />
        <el-table-column label="当前状态" prop="certStatus" show-overflow-tooltip>
          <template v-slot="scope">
            <el-tag size="small" v-if="scope.row.certStatus === certStatusEnum.OPEND" effect="dark" color="#52B81F">{{ certStatusMap[scope.row.certStatus] }}</el-tag>
            <el-tag size="small" v-else-if="scope.row.certStatus === certStatusEnum.OUTDATE" effect="dark" color="#787B84">{{ certStatusMap[scope.row.certStatus] }}</el-tag>
            <el-tag size="small" v-else-if="scope.row.certStatus === certStatusEnum.CLOSED" effect="dark" color="#FEA900">{{ certStatusMap[scope.row.certStatus] }}</el-tag>
            <el-tag size="small" v-else effect="dark" color="#3071F2">{{ certStatusMap[scope.row.certStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180px">
          <template v-slot="scope">
            <el-button @click="downloadCert(scope.row)" size="small" type="text">下载</el-button>
            <el-button @click="showConfirm(scope.row.certId, certUseStatusEnum.CLOSE)" v-if="scope.row.enable === certUseStatusEnum.OPEN" size="small" type="text">禁用</el-button>
            <el-button @click="showConfirm(scope.row.certId, certUseStatusEnum.OPEN)" v-if="scope.row.enable === certUseStatusEnum.CLOSE" size="small" type="text">启用</el-button>
            <el-button @click="modifyCertificate(scope.row)" size="small" type="text">更新</el-button>
            <el-button @click="showDelModal(scope.row)" size="small" type="text">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <we-pagination :total="total" :page_offset="pageData.page_offset" :page_size="pageData.page_size" @paginationChange="paginationHandle"></we-pagination>
    </div>
    <el-empty v-if="!total" :image-size="120" description="暂无数据">
      <img slot="image" src="~Assets/images/pic_empty_news.png" alt="" />
    </el-empty>
  </div>
</template>
<script>
import { certificateManageServer } from 'Api'
import { SET_USERINFO } from 'Store/mutation-types.js'
import { mapMutations, mapGetters } from 'vuex'
import { handleParamsValid } from 'Utils/index.js'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import { certStatusMap, certUseStatusEnum, certStatusEnum } from 'Utils/constant.js'
export default {
  name: 'groupManage',
  mixins: [tableHeightHandle],
  data() {
    return {
      searchForm: {
        agencyName: '',
        createTime: [],
        certStatus: ''
      },
      searchQuery: {
        agencyName: '',
        createTime: [],
        certStatus: ''
      },
      pageData: {
        page_offset: 1,
        page_size: 20
      },
      total: -1,
      queryFlag: false,
      tableData: [],
      loadingFlag: false,
      showAddModal: false,
      showChangeModal: false,
      groupId: '',
      userNameSelectList: [],
      groupAdminName: '',
      certStatusMap,
      certUseStatusEnum,
      certStatusEnum
    }
  },
  created() {
    this.getCertList()
  },
  computed: {
    ...mapGetters(['userinfo', 'userId', 'agencyList'])
  },
  methods: {
    ...mapMutations([SET_USERINFO]),
    newCertificate() {
      this.$router.push({ path: '/addCertificate' })
    },
    modifyCertificate(data) {
      const { certId } = data
      this.$router.push({ path: '/addCertificate', query: { certId, type: 'edit' } })
    },
    // 查询
    queryHandle() {
      this.$refs.searchForm.validate((valid) => {
        if (valid) {
          this.searchQuery = { ...this.searchForm }
          this.pageData.page_offset = 1
          this.getCertList()
        } else {
          return false
        }
      })
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.getCertList()
    },
    // 获取账户列表
    async getCertList() {
      const { page_offset, page_size } = this.pageData
      const { agencyName, certStatus, createTime } = this.searchQuery
      let params = handleParamsValid({ agencyName, certStatus })
      if (createTime && createTime.length) {
        params.signStartTime = createTime[0]
        params.signEndTime = createTime[1]
      }
      params = { ...params, pageNum: page_offset, pageSize: page_size }
      this.loadingFlag = true
      const res = await certificateManageServer.getCertList(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { agencyCertList = [], total } = res.data
        this.tableData = agencyCertList
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
      }
    },
    // 删除
    showDelModal(data) {
      const { certId } = data
      this.$confirm(`确认删除证书--'${certId}'?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.deleteCert({ certId })
        })
        .catch(() => {})
    },
    async deleteCert(params) {
      const res = await certificateManageServer.deleteCert(params)
      console.log(res)
      if (res.code === 0) {
        this.$message.success('证书删除成功')
        this.getCertList()
      }
    },
    showConfirm(certId, certStatus) {
      const msg = certStatus ? '证书禁用后将不可用，确定禁用吗？' : '证书启用后原有效期内可用，确定重新启用吗？'
      this.$confirm(msg, certStatus ? '确认禁用' : '确认启用', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.setCert(certId, certStatus)
        })
        .catch(() => {})
    },
    async setCert(certId, certStatus) {
      const res = await certificateManageServer.setCert({ certId, certStatus })
      console.log(res)
      if (res.code === 0) {
        const des = certStatus ? '禁用' : '启用'
        this.$message.success(`证书${des}成功`)
        this.getCertList()
      }
    },
    async downloadCert(params) {
      const { certId } = params
      const res = await certificateManageServer.downloadCert({ certId })
      console.log(res)
      if (res.code === 0) {
        const { certName, certScriptData } = res.data
        this.base64ToBlob(certScriptData, certName)
        this.$message.success('证书下载成功')
      }
    },
    base64ToBlob(base64String, certName) {
      const binaryString = window.atob(base64String)
      const len = binaryString.length
      const bytes = new Uint8Array(len)
      for (let i = 0; i < len; i++) {
        bytes[i] = binaryString.charCodeAt(i)
      }
      const blob = new Blob([bytes], {
        type: 'application/zip'
      })
      this.downloadZip(blob, certName)
    },
    downloadZip(blob, certName) {
      // 3. 创建下载链接并触发下载
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = certName
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      // 4. 清理
      URL.revokeObjectURL(url)
    },

    handlOK() {
      this.showAddModal = false
      this.showChangeModal = false
      this.getCertList()
    },
    reset() {
      this.$refs.searchForm.resetFields()
    }
  }
}
</script>
<style lang="less" scoped>
.cer-manage {
  ::v-deep .el-tag {
    padding: 0 12px;
    border: none;
    line-height: 24px;
  }
}
</style>
