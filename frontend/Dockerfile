FROM node:22-alpine AS build
WORKDIR /app
RUN corepack enable
COPY package.json yarn.lock ./
RUN yarn install --immutable
COPY . .
RUN yarn nx build matricula --configuration=production

FROM nginx:1.27-alpine
COPY --from=build /app/dist/apps/matricula/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80