#-- RUN STAGE
FROM nginx:stable

# Following file is essential to run nginx. .template used to ease substituting Environment variables
COPY nginx-template.conf /etc/nginx/nginx-template.conf

# Port in nginx (Configured in nginx-template.conf)
EXPOSE 8000

# Run nginx in the foreground
CMD ["/bin/sh", "-c", "envsubst '$$DOMAIN_IP $$FRONTEND_PORT' < /etc/nginx/nginx-template.conf > /etc/nginx/nginx.conf && nginx -g 'daemon off;'"]
#CMD ["nginx", "-g", "daemon off;"]
