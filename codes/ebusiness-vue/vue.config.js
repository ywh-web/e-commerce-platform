const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,

  // 关闭生产环境 source map，减小打包体积
  productionSourceMap: false,

  // 开发服务器配置
  devServer: {
    port: 8080,

    // 开发环境接口代理：把前端请求转发到后端 Spring Boot
    // 前端 → 后端：http://localhost:8443/eBusiness
    proxy: {
      // 1) 所有业务接口：/api/xxx → http://localhost:8443/eBusiness/api/xxx
      '/api': {
        target: 'http://localhost:8443/eBusiness',
        changeOrigin: true
      },
      // 2) 商品图片等上传资源：/upload/xxx.jpg
      //    → http://localhost:8443/eBusiness/upload/xxx.jpg
      '/upload': {
        target: 'http://localhost:8443/eBusiness',
        changeOrigin: true
      }
    }
  }
})
