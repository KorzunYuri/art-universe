FROM grafana/grafana:10.0.13
USER root
RUN apk add --no-cache gettext
USER grafana
