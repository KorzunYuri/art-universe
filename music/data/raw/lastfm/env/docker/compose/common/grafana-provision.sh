#!/bin/sh

echo "Starting Grafana provisioning..."

# Enable error handling
set -e

# Check if required directories exist
echo "Checking required directories..."
if [ ! -d "/tmp/dashboard-templates" ]; then
    echo "ERROR: /tmp/dashboard-templates directory not found"
    exit 1
fi

if [ ! -d "/tmp/datasource-templates" ]; then
    echo "ERROR: /tmp/datasource-templates directory not found"
    exit 1
fi

# List available files for debugging
echo "Available dashboard templates:"
ls -la /tmp/dashboard-templates/

echo "Available datasource templates:"
ls -la /tmp/datasource-templates/

# Create directories
echo "Creating provisioning directories..."
mkdir -p /etc/grafana/provisioning/dashboards
mkdir -p /etc/grafana/provisioning/datasources

# Copy dashboard configuration
echo "Copying dashboard configuration..."
if [ -f "/tmp/dashboard-templates/dashboard.yml" ]; then
    cp /tmp/dashboard-templates/dashboard.yml /etc/grafana/provisioning/dashboards/
    echo "Dashboard configuration copied successfully"
else
    echo "WARNING: dashboard.yml not found"
fi

# Generate dashboards from templates
echo "Generating dashboards from templates..."
if [ -f "/tmp/dashboard-templates/lastfm_raw_data_dashboard.json.template" ]; then
    envsubst < /tmp/dashboard-templates/lastfm_raw_data_dashboard.json.template > /etc/grafana/provisioning/dashboards/lastfm_raw_data_dashboard.json
    echo "Raw data dashboard generated successfully"
else
    echo "WARNING: lastfm_raw_data_dashboard.json.template not found"
fi

if [ -f "/tmp/dashboard-templates/lastfm_system_metrics_dashboard.json.template" ]; then
    envsubst < /tmp/dashboard-templates/lastfm_system_metrics_dashboard.json.template > /etc/grafana/provisioning/dashboards/lastfm_system_metrics_dashboard.json
    echo "System metrics dashboard generated successfully"
else
    echo "WARNING: lastfm_system_metrics_dashboard.json.template not found"
fi

if [ -f "/tmp/dashboard-templates/lastfm_database_metrics_dashboard.json.template" ]; then
    envsubst < /tmp/dashboard-templates/lastfm_database_metrics_dashboard.json.template > /etc/grafana/provisioning/dashboards/lastfm_database_metrics_dashboard.json
    echo "Database metrics dashboard generated successfully"
else
    echo "WARNING: lastfm_database_metrics_dashboard.json.template not found"
fi

# Generate datasource from template
echo "Generating datasource configuration..."
if [ -f "/tmp/datasource-templates/datasource.yml.template" ]; then
    envsubst < /tmp/datasource-templates/datasource.yml.template > /etc/grafana/provisioning/datasources/datasource.yml
    echo "Datasource configuration generated successfully"
else
    echo "ERROR: datasource.yml.template not found"
    exit 1
fi

echo "Provisioning completed. Starting Grafana..."

# Start Grafana with original entrypoint
exec /run.sh "$@"
