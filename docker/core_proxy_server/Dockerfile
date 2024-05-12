FROM nginx:alpine3.18

COPY docker/core_proxy_server/etc/nginx/nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
EXPOSE 8081

CMD ["nginx", "-g", "daemon off;"]
