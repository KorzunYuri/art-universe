#!/bin/sh
# Generate runtime config from environment variables
envsubst < /usr/share/nginx/html/config.js.template > /usr/share/nginx/html/config.js
exec nginx -g 'daemon off;'
