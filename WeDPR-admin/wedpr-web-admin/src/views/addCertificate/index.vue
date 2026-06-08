<template>
  <div class="create-cer">
    <el-form :inline="false" @submit="queryHandle" :rules="rules" :model="dataForm" ref="dataForm" size="small">
      <el-form-item v-if="certId" label-width="124px" label="证书编号：" prop="certId">
        <el-input disabled style="width: 480px" placeholder="请输入" v-model="dataForm.certId" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label-width="124px" label="证书请求文件：" prop="csrFile">
        <weUpLoad key="dataCsvFile" accept=".csr" tips="将证书文件拖到此处，或点击此处上传" :beforeUpload="beforeUploadCsv" v-model="dataForm.csrFile">
          <p @click.stop="downloadCertTool" class="templateTips">下载证书工具</p>
        </weUpLoad>
      </el-form-item>
      <el-form-item prop="agencyName" label="绑定机构：" label-width="124px">
        <el-select :disabled="Boolean(certId)" size="small" style="width: 480px" v-model="dataForm.agencyName" placeholder="请选择">
          <el-option :key="item" v-for="item in showAgencyList" :label="item.label" :value="item.value"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item prop="expireTime" label="设置有效期：" label-width="124px">
        <el-date-picker :picker-options="pickerOptions" style="width: 480px" value-format="yyyy-MM-dd" v-model="dataForm.expireTime" placeholder="请选择有效期" type="date" />
      </el-form-item>
    </el-form>
    <div style="padding-left: 124px">
      <el-button v-if="certId" size="medium" icon="el-icon-edit" type="primary" @click="submit"> 确认更新 </el-button>
      <el-button v-if="!certId" size="medium" icon="el-icon-plus" type="primary" @click="submit"> 确认创建 </el-button>
    </div>
  </div>
</template>
<script>
import { certificateManageServer } from 'Api'
import weUpLoad from '@/components/upLoad.vue'
import { mapGetters } from 'vuex'
export default {
  name: 'certCreate',
  data() {
    return {
      dataForm: {
        certId: '',
        csrFile: '',
        agencyName: '',
        expireTime: ''
      },
      rules: {
        certId: [{ required: true, message: '证书编号不能为空', trigger: 'blur' }],
        agencyName: [{ required: true, message: '绑定机构不能为空', trigger: 'blur' }],
        csrFile: [{ required: true, message: '证书请求文件不能为空', trigger: 'blur' }],
        expireTime: [{ required: true, message: '有效期不能为空', trigger: 'blur' }]
      },
      certId: '',
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() + 86400000
        }
      },
      showAgencyList: []
    }
  },
  components: {
    weUpLoad
  },
  computed: {
    ...mapGetters(['agencyList'])
  },
  created() {
    const { certId } = this.$route.query
    this.showAgencyList = this.agencyList
    this.getNoCertAgencyList()
    if (certId) {
      this.dataForm.certId = certId
      this.certId = certId
      this.getCertDetail()
    }
  },
  methods: {
    // 获取证书详情
    async getCertDetail() {
      this.loadingFlag = true
      const { certId } = this
      const res = await certificateManageServer.getCertDetail({ certId })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { certId, csrFile, expireTime, agencyName, csrFileName } = res.data
        this.dataForm = { certId, expireTime, agencyName }
        this.dataForm.csrFile = this.base64ToBlob(csrFile, csrFileName)
        console.log(this.dataForm.csrFile)
      }
    },
    // 获取无证书机构
    async getNoCertAgencyList() {
      const res = await certificateManageServer.getNoCertAgencyList()
      console.log(res)
      if (res.code === 0 && res.data) {
        const { agencyList } = res.data
        this.showAgencyList = agencyList.map((v) => {
          return {
            label: v.agencyName,
            value: v.agencyName
          }
        })
      }
    },
    // 获取证书模板
    async downloadCertTool() {
      const res = await certificateManageServer.downloadCertTool()
      console.log(res)
      if (res.code === 0 && res.data) {
        console.log(res)
        const { certToolName, certToolData } = res.data
        this.downloadZipByBase64String(certToolData, certToolName)
        this.$message.success('证书工具下载成功')
      }
    },
    base64ToBlob(base64String, csrFileName) {
      const binaryString = window.atob(base64String)
      const len = binaryString.length
      const bytes = new Uint8Array(len)
      for (let i = 0; i < len; i++) {
        bytes[i] = binaryString.charCodeAt(i)
      }
      const blob = new Blob([bytes], {
        type: 'application/csr'
      })
      return new File([blob], csrFileName)
    },
    downloadZipByBase64String(base64String, zipName) {
      const binaryString = window.atob(base64String)
      const len = binaryString.length
      const bytes = new Uint8Array(len)
      for (let i = 0; i < len; i++) {
        bytes[i] = binaryString.charCodeAt(i)
      }
      const blob = new Blob([bytes], {
        type: 'application/zip'
      })
      this.downloadZip(blob, zipName)
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
    async createCertificate(params) {
      const res = await certificateManageServer.createCert(params)
      console.log(res)
      if (res.code === 0) {
        this.$message.success('证书创建成功')
        this.$router.push({ path: '/certificateManage' })
      }
    },
    async modifyCertificate(params) {
      const res = await certificateManageServer.createCert(params)
      console.log(res)
      if (res.code === 0) {
        this.$message.success('证书更新成功')
        history.go(-1)
      }
    },
    submit() {
      this.$refs.dataForm.validate((valid) => {
        if (valid) {
          const { csrFile, expireTime = '', agencyName = '' } = this.dataForm
          console.log(csrFile, 'csrFile', expireTime, agencyName)
          const form = new FormData()
          if (expireTime.indexOf(':') > -1) {
            form.append('expireTime', expireTime)
          } else {
            form.append('expireTime', expireTime + ' 00:00:00')
          }
          form.append('csrFile', csrFile)
          if (this.certId) {
            form.append('certId', this.certId)
            this.modifyCertificate(form)
          } else {
            form.append('agencyName', agencyName)
            this.createCertificate(form)
          }
        }
      })
    }
  }
}
</script>
<style lang="less" scoped>
div.create-cer {
  .el-checkbox {
    display: block;
    margin-bottom: 16px;
  }
  .templateTips {
    color: #3071f2;
    font-size: 12px;
    margin-left: 8px;
    transform: translateY(3px);
    cursor: pointer;
  }
}
</style>
