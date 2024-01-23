# 使用OpenEuler作为基础镜像
FROM openeuler/openeuler:20.03

# 安装Nginx 1.21.5
RUN yum install -y nginx-1.21.5

# 安装PHP 5.6.6
RUN yum install -y php-5.6.6 php-fpm-5.6.6

# 删除默认Nginx配置
RUN rm -f /etc/nginx/conf.d/default.conf

# 添加自定义Nginx配置文件来阻止PHP
COPY nginx.conf /etc/nginx/conf.d/nginx.conf

# 暴露Nginx的HTTP端口
EXPOSE 80

# 启动Nginx和PHP-FPM
CMD ["nginx", "-g", "daemon off;"]
