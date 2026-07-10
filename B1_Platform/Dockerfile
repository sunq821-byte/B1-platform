FROM nginx:latest

# 替换默认站点配置
RUN rm -f /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/b1.conf

# 预构建产物：开发机 npm run build 产出的静态资源
COPY dist /usr/share/nginx/html

EXPOSE 80
