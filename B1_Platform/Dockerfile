ARG NODE_IMAGE=loongarch64/node:20
FROM ${NODE_IMAGE} AS build

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

ARG NGINX_IMAGE=loongarch64/nginx
FROM ${NGINX_IMAGE}

RUN rm -f /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/b1.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
