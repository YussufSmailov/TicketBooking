# Сборка сайта. Адрес API вшивается в сборку, поэтому VITE_API_URL нужен уже здесь.
FROM node:20.18.1-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL
RUN test -n "$VITE_API_URL" || (echo "Задайте переменную VITE_API_URL, например https://backend-xxx.up.railway.app/api" && exit 1)
RUN npm run build

# Раздача готовых файлов через nginx. Порт берётся из переменной PORT (её задаёт Railway).
FROM nginx:1.27.3-alpine
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
ENV PORT=8080
EXPOSE 8080
