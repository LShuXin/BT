FROM alpine:latest
RUN apk add --no-cache nginx
COPY docker/web_client/nginx.conf /etc/nginx/nginx.conf
COPY web_client /usr/share/nginx/html
CMD ["nginx", "-g", "daemon off;"]