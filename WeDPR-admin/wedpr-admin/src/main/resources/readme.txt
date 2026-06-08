1.安装依赖
安装macOS依赖
# 最新homebrew默认下载的为openssl@3，需要指定版本openssl@1.1下载
brew install openssl@1.1 curl

安装ubuntu依赖
sudo apt install -y curl openssl

安装centos依赖
sudo yum install -y curl openssl openssl-devel

2.解压cert_tool.zip文件
unzip cert_tool.zip && cd cert_tool

3.站点端生成机构证书请求文件：生成的agency.csr在给定的机构名目录下
# bash cert_script.sh gen_rsa_req agency $机构名
例如: bash cert_script.sh gen_rsa_req agency webank