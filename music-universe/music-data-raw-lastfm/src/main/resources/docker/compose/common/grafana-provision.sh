#!/bin/sh

echo "Starting Grafana provisioning..."

# Create directories
mkdir -p /etc/grafana/provisioning/dashboards
mkdir -p /etc/grafana/provisioning/datasources

# Copy dashboard configuration
cp /tmp/dashboard-templates/dashboard.yml /etc/grafana/provisioning/dashboards/

# Generate dashboards from templates
envsubst < /tmp/dashboard-templates/lastfm_raw_data_dashboard.json.template > /etc/grafana/provisioning/dashboards/lastfm_raw_data_dashboard.json
envsubst < /tmp/dashboard-templates/lastfm_system_metrics_dashboard.json.template > /etc/grafana/provisioning/dashboards/lastfm_system_metrics_dashboard.json

# Generate datasource from template
envsubst < /tmp/datasource-templates/datasource.yml.template > /etc/grafana/provisioning/datasources/datasource.yml

echo "Provisioning completed. Starting Grafana..."

# Start Grafana with original entrypoint
exec /run.sh "$@"
