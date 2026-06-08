const { defineConfig } = require('@vue/cli-service')

const path = require('path') // 引入path模块
function resolve(dir) {
  return path.join(__dirname, dir) // path.join(__dirname)设置绝对路径
}

module.exports = defineConfig({
  outputDir: 'manage',
  transpileDependencies: true,
  parallel: true,
  publicPath: './',
  configureWebpack: (config) => {
    // 调试JS
    process.env.NODE_ENV !== 'production' && (config.devtool = 'source-map')
  },
  css: {
    loaderOptions: {
      sass: {
        // 注意： sass-loader v8 以上版本，这个选项名是 prependData
        //       sass-loader v8 以下版本，这个选项名是 additionalData
        additionalData: "$--font-path: '~element-ui/lib/theme-chalk/fonts'; @import './src/assets/style/variables.scss';"
      }
    }
  },
  devServer: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:6850',
        secure: false,
        changeOrigin: true
      }
    },
    host: '0.0.0.0',
    allowedHosts: 'all',
    port: 3001,
    client: {
      webSocketURL: 'auto://0.0.0.0:0/ws'
    }
  },
  chainWebpack: (config) => {
    config.resolve.alias
      .set('@', resolve('src'))
      .set('Components', resolve('.src/components'))
      .set('Assets', resolve('src/assets'))
      .set('Api', resolve('./src/apis'))
      .set('Utils', resolve('./src/utils'))
      .set('Mixin', resolve('./src/mixin'))
      .set('Store', resolve('./src/store'))
  }
})
