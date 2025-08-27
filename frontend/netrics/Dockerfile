#-- BUILD STAGE
FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
RUN npm install @angular/cli
COPY . .

#To avoid ng not found error
RUN npm link @angular/cli

RUN ng build --configuration=production

#-- RUN STAGE
FROM nginx:latest

# Following file is essential to run nginx
COPY nginx.conf /etc/nginx/nginx.conf

COPY --from=build app/dist/kpi-frontend/browser /usr/share/nginx/html/

# Default port in nginx (Configured in nginx.conf). Use 4200:80 when running docker container [dockre run -p 4200:80 ...]
EXPOSE 80
