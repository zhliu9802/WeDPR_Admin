<template>
  <div>
    <!-- 上传完成 -->
    <div :class="className" v-if="showInfo">
      <div class="title">
        <img src="~Assets/images/icon_data.png" alt="" />
        <span :title="dataInfo.datasetTitle">{{ dataInfo.datasetTitle }}</span>
      </div>
      <ul @click="goDetail">
        <li>
          数据量<span class="data-size">
            <i>{{ dataInfo.recordCount }}</i
            >*<i>{{ dataInfo.columnCount }}</i></span
          >
        </li>
        <li v-if="showOwner">
          所属用户 <span :title="dataInfo.ownerUserName">{{ dataInfo.ownerUserName }}</span>
        </li>
        <li>
          所属机构 <span :title="dataInfo.ownerAgencyName">{{ dataInfo.ownerAgencyName }}</span>
        </li>
        <li>
          数据来源 <span :title="dataInfo.ownerAgencyName">{{ dataInfo.dataSourceType }}</span>
        </li>
        <li>
          创建时间 <span :title="dataInfo.createAt">{{ dataInfo.createAt }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import { downloadLargeFile } from 'Mixin/downloadLargeFile.js'
import { mapGetters } from 'vuex'
import { dataStatusEnum } from 'Utils/constant.js'
export default {
  name: 'dataCard',
  mixins: [downloadLargeFile],
  props: {
    dataInfo: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {}
  },
  computed: {
    ...mapGetters(['fileUploadTask']),
    className() {
      let name = this.dataInfo.isOwner ? 'data-card' : 'others data-card'
      name += this.selected ? ' selected' : ''
      return name
    },
    showInfo() {
      return this.dataInfo.status === dataStatusEnum.Success
    }
  },
  methods: {
    goDetail() {
      if (!this.dataInfo.status) {
        this.$emit('getDetail')
      }
    }
  }
}
</script>

<style scoped lang="less">
div.data-card {
  background: #f6fcf9;
  // width: 314px;
  max-height: 280px;
  border: 1px solid #e0e4ed;
  border-radius: 12px;
  margin: 16px;
  width: calc(25% - 32px);
  box-sizing: border-box;
  min-width: 220px;
  padding: 20px;
  position: relative;
  overflow: hidden;
  float: left;
  ::v-deep .el-checkbox__inner {
    border-radius: 50%;
    width: 20px;
    height: 20px;
    line-height: 20px;
    font-size: 16px;
  }
  ::v-deep .el-checkbox__inner::after {
    left: 7px;
    width: 4px;
    height: 8px;
    top: 3px;
  }
  div.title {
    font-size: 16px;
    line-height: 24px;
    font-family: PingFang SC;
    display: flex;
    align-items: center;
    margin-bottom: 24px;
    color: #262a32;
    img {
      width: 24px;
      height: 24px;
    }
    span {
      text-align: left;
      flex: 1;
      font-weight: bold;
      overflow: hidden;
      text-overflow: ellipsis;
      text-indent: 8px;
      white-space: nowrap;
    }
    span.auth {
      position: absolute;
      right: 0;
      top: 0;
      background: #52b81f;
      color: white;
      padding: 2px 6px;
      font-size: 12px;
      line-height: 16px;
      border-radius: 4px;
    }
    ::v-deep .el-checkbox__inner {
      border: 1px solid #3071f2;
      box-shadow: 0 0 3px #3071f2;
    }
  }
  ul {
    li {
      font-size: 12px;
      line-height: 24px;
      margin-bottom: 6px;
      color: #787b84;
      display: flex;
      span {
        flex: 1;
        text-align: right;
        color: #262a32;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        padding-left: 5px;
      }
      span.data-size {
        i {
          font-size: 12px;
          font-style: normal;
        }
      }
    }
    li:last-child {
      margin-bottom: 0;
    }
  }
  div.edit {
    margin-top: 28px;
  }
  .op-con {
    display: flex;
    justify-content: space-between;
    height: auto;
    img {
      width: 24px;
      height: 24px;
      cursor: pointer;
    }
  }
  div.apply {
    border: 1px solid #b3b5b9;
    margin-bottom: -4px;
    margin-top: -4px;
    height: 32px;
    padding: 5px 12px;
    text-align: center;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    justify-content: center;
    span {
      display: flex;
      align-items: center;
    }
    img {
      width: 16px;
      height: 16px;
      margin-right: 8px;
    }
  }
  div.apply.authed {
    cursor: default;
    border: 1px solid #e0e4ed;
    color: #b3b5b9;
  }
  div.apply.delete {
    color: #ff4d4f;
    border: 1px solid #e0e4ed;
  }
  div.apply.reupload {
    border: 1px solid #e0e4ed;
    margin-bottom: 16px;
  }
  div.fail {
    color: #787b84;
    text-align: center;
    font-size: 14px;
    padding-top: 42px;
    padding-bottom: 32px;
    i {
      color: #fea900;
      font-size: 18px;
      transform: translateY(2px);
      margin-right: 6px;
    }
  }
  div.tag {
    width: 52px;
    height: 20px;
    line-height: 20px;
    font-size: 12px;
    background-color: #52b81f;
    color: white;
    border-top-right-radius: 4px;
    border-bottom-left-radius: 4px;
    text-align: center;
    margin-right: 12px;
    margin-left: 6px;
  }
}
div.data-card.others {
  background: #f6f8fc;
  border: 1px solid #e0e4ed;
}
div.data-card:hover {
  box-shadow: 0px 4px 10px 4px #00000014;
  cursor: pointer;
}
div.data-card.selected {
  box-shadow: 0px 4px 10px 4px #00000014;
}
</style>
