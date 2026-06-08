<template>
  <div class="agency-manage">
    <div class="form-search">
      <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
        <el-form-item>
          <el-button type="primary" icon="el-icon-plus" @click="goAdd"> 新增机构 </el-button>
        </el-form-item>
        <div style="float: right">
          <el-form-item prop="agencyName" label="机构名称：">
            <el-select clearable size="small" style="width: 160px" v-model="searchForm.agencyName" placeholder="请选择">
              <el-option v-for="item in agencyList" :label="item.label" :value="item.label" :key="item.label"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item prop="agencyId" label="机构编号：">
            <el-select loading-text="搜索中" filterable remote :remote-method="getAgencyIdSelect" style="width: 200px" v-model="searchForm.agencyId" placeholder="请选择" clearable>
              <el-option v-for="item in agencyIdSelectList" :label="item.label" :value="item.value" :key="item.value"></el-option>
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
    <div class="tableContent autoTableWrap">
      <el-table :max-height="tableHeight" size="small" v-loading="queryFlag" :data="tableData" :border="true" class="table-wrap">
        <el-table-column label="机构编号" width="180px" prop="agencyId" show-overflow-tooltip />
        <el-table-column label="机构名称" prop="agencyName" show-overflow-tooltip />
        <el-table-column label="机构联系人" prop="agencyContact" show-overflow-tooltip />
        <el-table-column label="联系方式" prop="contactPhone" show-overflow-tooltip />
        <el-table-column label="注册时间" prop="createTime" show-overflow-tooltip />
        <el-table-column label="机构内用户数" prop="userCount" show-overflow-tooltip />
        <el-table-column label="机构状态" prop="agencyStatus">
          <template v-slot="scope">
            <el-switch
              class="scopeSwitch"
              :value="!scope.row.agencyStatus"
              @change="(status) => handleStatusChange(status, scope.row)"
              inactive-color="#787B84"
              active-text="启用"
              inactive-text="禁用"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="证书状态" prop="certStatus">
          <template v-slot="scope">
            <el-tag size="small" v-if="scope.row.certStatus === 0" effect="dark" style="color: #262a32" color="#E0E4ED">{{ certStatusMap[scope.row.certStatus] }}</el-tag>
            <el-tag size="small" v-else-if="scope.row.certStatus === 1" effect="dark" color="#52B81F">{{ certStatusMap[scope.row.certStatus] }}</el-tag>
            <el-tag size="small" v-else-if="scope.row.certStatus === 2" effect="dark" color="#787B84">{{ certStatusMap[scope.row.certStatus] }}</el-tag>
            <el-tag size="small" v-else-if="scope.row.certStatus === 3" effect="dark" color="#FEA900">{{ certStatusMap[scope.row.certStatus] }}</el-tag>
            <el-tag size="small" v-else effect="dark" color="#3071F2">{{ certStatusMap[scope.row.certStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="224px">
          <template v-slot="scope">
            <el-button @click="modifyDetail(scope.row)" size="small" type="text">修改</el-button>
            <el-button v-if="userinfo.roleName === 'agency_admin'" @click="showDelModal(scope.row)" size="small" type="text">删除</el-button>
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
import { agencyManageServer } from 'Api'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import { handleParamsValid } from 'Utils/index.js'
import { certStatusMap, agencyStatusEnum } from 'Utils/constant.js'
import { mapGetters, mapMutations } from 'vuex'
import { SET_AGENCYLIST } from 'Store/mutation-types.js'
export default {
  name: 'agencyManage',
  mixins: [tableHeightHandle],
  data() {
    return {
      searchForm: {
        agencyName: '',
        agencyId: ''
      },
      searchQuery: {
        agencyName: '',
        agencyId: ''
      },
      pageData: {
        page_offset: 1,
        page_size: 10
      },
      total: -1,
      queryFlag: false,
      tableData: [],
      showAddModal: false,
      showChangeModal: false,
      agencyIdSelectList: [],
      modifyagencyId: '',
      certStatusMap,
      agencyStatusEnum
    }
  },
  created() {
    this.getAgencyList()
    this.getAgencyListSelect()
  },
  computed: {
    ...mapGetters(['userinfo', 'userId', 'agencyList'])
  },
  methods: {
    ...mapMutations([SET_AGENCYLIST]),
    handleStatusChange(agencyStatus, data) {
      const { agencyId } = data
      data.agencyStatus = agencyStatus ? 0 : 1
      this.setAgencyStatus({ agencyId, agencyStatus: agencyStatus ? agencyStatusEnum.OPEN : agencyStatusEnum.CLOSE })
    },
    // 查询
    queryHandle() {
      this.$refs.searchForm.validate((valid) => {
        if (valid) {
          this.searchQuery = { ...this.searchForm }
          this.pageData.page_offset = 1
          this.getAgencyList()
        } else {
          return false
        }
      })
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.getAgencyList()
    },
    // 获取机构列表全量
    async getAgencyListSelect() {
      const params = { pageNum: 1, pageSize: 999 }
      const res = await agencyManageServer.getAgencyList(params)
      console.log(res)
      if (res.code === 0 && res.data) {
        const { wedprAgencyDTOList = [] } = res.data
        const agencyListSelect = wedprAgencyDTOList.map((v) => {
          return {
            label: v.agencyName,
            value: v.agencyName
          }
        })
        this.SET_AGENCYLIST(agencyListSelect)
      }
    },
    async getAgencyIdSelect(agencyId) {
      if (!agencyId) {
        this.agencyIdSelectList = []
        return
      }
      const res = await agencyManageServer.getAgencyList({ pageSize: 9999, pageNum: 1, agencyId })
      if (res.code === 0 && res.data) {
        const { wedprAgencyDTOList = [] } = res.data
        this.agencyIdSelectList = wedprAgencyDTOList.map((v) => {
          return {
            label: v.agencyId,
            value: v.agencyId
          }
        })
      }
    },
    // 获取机构列表
    async getAgencyList() {
      const { page_offset, page_size } = this.pageData
      const { agencyName = '', agencyId = '' } = this.searchQuery
      let params = handleParamsValid({ agencyName, agencyId })
      params = { ...params, pageNum: page_offset, pageSize: page_size }
      this.queryFlag = true
      const res = await agencyManageServer.getAgencyList(params)
      this.queryFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { wedprAgencyDTOList = [], total } = res.data
        this.tableData = wedprAgencyDTOList
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
      }
    },
    // 删除机构
    showDelModal(agency) {
      const { agencyId = '', agencyName } = agency
      this.$confirm(`确认删除机构--'${agencyName}'?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.deleteAgency({ agencyId })
        })
        .catch(() => {})
    },
    async deleteAgency(params) {
      const res = await agencyManageServer.deleteAgency(params)
      console.log(res)
      if (res.code === 0) {
        this.$message.success('机构删除成功')
        this.getAgencyList()
        this.getAgencyListSelect()
      }
    },
    async setAgencyStatus(params) {
      const res = await agencyManageServer.setAgency(params)
      console.log(res)
      if (res.code === 0) {
        const { agencyStatus } = params
        this.$message.success('机构' + (agencyStatus ? '禁用' : '启用') + '成功')
        this.getAgencyList()
      }
    },
    goAdd() {
      this.$router.push({ path: 'agencyCreate' })
    },
    closeModal() {
      this.showAddModal = false
    },
    closeChangeModal() {
      this.showChangeModal = false
    },
    handlOK() {
      this.showAddModal = false
      this.showChangeModal = false
      this.getAgencyList()
    },
    modifyDetail(data) {
      this.$router.push({ path: 'agencyCreate', query: { agencyId: data.agencyId, type: 'edit' } })
    },
    reset() {
      this.$refs.searchForm.resetFields()
    }
  }
}
</script>
<style lang="less" scoped>
.agency-manage {
  ::v-deep .el-tag {
    padding: 0 12px;
    border: none;
    line-height: 24px;
  }
}
</style>
