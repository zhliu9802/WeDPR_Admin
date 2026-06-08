<template>
  <div class="login">
    <div class="login-content">
      <img class="login-left" src="~Assets/images/admin_login.png" />
      <div class="login-right">
        <div class="welcome-info">管理台登录</div>
        <div class="login-form">
          <el-form ref="adminForm" :model="adminForm" :rules="adminFormRules" @keydown.enter.native="handleSubmit">
            <el-form-item prop="username">
              <el-input type="text" v-model="adminForm.username" placeholder="请输入管理台账号"> </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input type="password" v-model="adminForm.password" placeholder="请输入登录密码" show-password> </el-input>
            </el-form-item>
            <el-form-item prop="imageCode">
              <el-input style="width: 244px" type="input" v-model="adminForm.imageCode" placeholder="请输入验证码"> </el-input>
              <img class="code" @click="getImageCode" :src="imageBase64" alt="" />
            </el-form-item>
            <el-form-item>
              <el-button class="sub" style="width: 100%" @click="handleSubmit" type="primary" long>登录</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapMutations, mapGetters } from 'vuex'
import {
  SET_GROUPLIST,
  SET_ALGLIST,
  SET_AGENCYLIST,
  SET_AUTHORIZATION,
  SET_PERMISSION,
  SET_USERINFO,
  SET_USERID,
  SET_AGENCYID,
  SET_AGENCYNAME,
  SET_AGENCYADMIN
} from 'Store/mutation-types.js'
import { loginManageServer, settingManageServer, agencyManageServer } from 'Api'
import { permissionMap } from 'Utils/config.js'
import { jwtDecode } from 'jwt-decode'
const sm2 = require('sm-crypto').sm2
export default {
  data() {
    return {
      adminForm: {
        username: '',
        password: '',
        imageCode: ''
      },
      imageBase64: '',
      randomToken: '',
      adminFormRules: {
        username: [{ required: true, message: '管理员账号不能为空', trigger: 'blur' }],
        password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
        imageCode: [{ required: true, message: '验证码不能为空', trigger: 'blur' }]
      }
    }
  },
  components: {},
  created() {
    this.getImageCode()
  },
  computed: {
    ...mapGetters(['pbKey', 'permission'])
  },
  methods: {
    ...mapMutations([SET_GROUPLIST, SET_ALGLIST, SET_AGENCYLIST, SET_AUTHORIZATION, SET_PERMISSION, SET_USERINFO, SET_USERID, SET_AGENCYID, SET_AGENCYNAME, SET_AGENCYADMIN]),
    async getImageCode() {
      const res = await settingManageServer.getImageCode()
      if (res.code === 0 && res.data) {
        const { imageBase64, randomToken } = res.data
        this.randomToken = randomToken
        this.imageBase64 = `data:image/png;base64,${imageBase64}`
      }
    },
    encodePassword(password) {
      const { pbKey } = this
      const cipherMode = 1
      const encryptedPassword = sm2.doEncrypt(password, pbKey, cipherMode)
      return encryptedPassword
    },
    async normalLogin(params) {
      const res = await loginManageServer.adminLogin(params)
      console.log(res)
      if (res.code === 0) {
        this.$message({ type: 'success', message: '登录成功!' })
        const { jwt } = res.data
        console.log(res)
        const { user: userData } = jwtDecode(jwt)
        console.log(JSON.parse(userData), 'JSON.parse(userData)')
        const { username, roleName } = JSON.parse(userData)
        this.SET_AUTHORIZATION(jwt)
        this.SET_USERID(username)
        this.SET_USERINFO(JSON.parse(userData))
        this.SET_GROUPLIST([])
        console.log(permissionMap[roleName], 'permissionMap[roleName]', roleName)
        this.SET_PERMISSION(permissionMap[roleName])
        console.log(this.permission, 'permission')
        console.log(roleName, 'roleName')
        this.getAgencyList()
      }
    },
    // 登录提交
    handleSubmit() {
      this.$refs.adminForm.validate((valid) => {
        if (valid) {
          const { adminForm } = this
          const { password } = adminForm
          //  FIXME:
          this.normalLogin({ ...adminForm, password: this.encodePassword(password), randomToken: this.randomToken })
        }
      })
    },
    // 获取机构列表
    async getAgencyList() {
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
        await this.getConfigAlgList()
      }
    },
    async getConfigAlgList() {
      const res = await settingManageServer.adminGetConfig({ key: 'wedpr_algorithm_templates' })
      if (res.code === 0 && res.data) {
        const realData = JSON.parse(res.data)
        const algList = realData.templates.map((v) => {
          return {
            ...v,
            label: v.title,
            value: v.name,
            src: require('../../assets/images/alg/' + v.name + '.png')
          }
        })
        this.SET_ALGLIST(algList)
        const redirectUrl = this.$route.query.redirectUrl
        const targetPath = redirectUrl ? decodeURIComponent(redirectUrl) : '/dataManage'
        this.$router.push({ path: targetPath }).catch(() => {})
      }
    }
  }
}
</script>
<style lang="less" scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  min-width: 1280px;
  min-height: 720px;
  width: 100%;
  height: 100%;
  position: relative;
  box-sizing: border-box;
  background-image: url('~Assets/images/bg.png');
  background-size: 100% 100%;
  min-width: 1600px;
  .code {
    width: auto;
    margin-left: 10px;
    height: 38px;
    vertical-align: middle;
  }
  .login-content {
    display: flex;
    align-items: center;
    padding: 0 180px 0 250px;
    justify-content: space-between;
    width: 100%;
  }
  .login-left {
    width: 45%;
    height: auto;
  }
  .login-right {
    width: 440px;
    // height: 496px;
    height: auto;
    padding: 60px 40px;
    padding-bottom: 76px;
    border-radius: 12px;
    border: 1px solid #e0e4ed;
    box-shadow: 0px 4px 20px 2px #2e363f14;
    .welcome-info {
      color: #262a32;
      font-size: 24px;
    }
    .login-form {
      margin-top: 60px;
    }
    p.tips {
      font-size: 14px;
      line-height: 22px;
      margin-bottom: 16px;
      text-align: center;
      cursor: pointer;
      color: #3071f2;
      margin-top: 16px;
    }
    p.title {
      color: #787b84;
      margin-bottom: 8px;
      span {
        color: #3071f2;
        cursor: pointer;
      }
    }
  }
}
</style>
