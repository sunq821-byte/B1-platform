ARG NGINX_IMAGE=lcr.loongnix.cn/library/nginx:latest
FROM ${NGINX_IMAGE}

RUN rm -f /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/b1.conf
COPY dist /usr/share/nginx/html
EXPOSE 80
