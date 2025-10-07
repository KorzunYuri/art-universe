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

if [ -f "/tmp/dashboard-templates/lastfm_database_metrics_dashboard.json.template" ]; then
    envsubst < /tmp/dashboard-templates/lastfm_database_metrics_dashboard.json.template > /etc/grafana/provisioning/dashboards/lastfm_database_metrics_dashboard.json
    echo "Database metrics dashboard generated successfully"
else
    echo "WARNING: lastfm_database_metrics_dashboard.json.template not found"
fi

# Generate service-specific dashboards from universal template
if [ -f "/tmp/dashboard-templates/service_system_metrics_dashboard.json.template" ]; then
    # Music Data
    export SERVICE_ID=5
    export SERVICE_JOB="music-data"
    export SERVICE_TITLE="Music Data System Metrics"
    export SERVICE_UID="music-data-system-metrics"
    envsubst < /tmp/dashboard-templates/service_system_metrics_dashboard.json.template > /etc/grafana/provisioning/dashboards/music_data_system_metrics_dashboard.json
    echo "Music Data system metrics dashboard generated successfully"
    
    # Music Quiz
    export SERVICE_ID=6
    export SERVICE_JOB="music-quiz"
    export SERVICE_TITLE="Music Quiz System Metrics"
    export SERVICE_UID="music-quiz-system-metrics"
    envsubst < /tmp/dashboard-templates/service_system_metrics_dashboard.json.template > /etc/grafana/provisioning/dashboards/music_quiz_system_metrics_dashboard.json
    echo "Music Quiz system metrics dashboard generated successfully"
    
    # LastFM REST API
    export SERVICE_ID=7
    export SERVICE_JOB="lastfm-rest-api"
    export SERVICE_TITLE="LastFM REST API System Metrics"
    export SERVICE_UID="lastfm-rest-api-system-metrics"
    envsubst < /tmp/dashboard-templates/service_system_metrics_dashboard.json.template > /etc/grafana/provisioning/dashboards/lastfm_rest_api_system_metrics_dashboard.json
    echo "LastFM REST API system metrics dashboard generated successfully"
    
    # LastFM ETL REST API
    export SERVICE_ID=8
    export SERVICE_JOB="lastfm-etl-rest-api"
    export SERVICE_TITLE="LastFM ETL REST API System Metrics"
    export SERVICE_UID="lastfm-etl-rest-api-system-metrics"
    envsubst < /tmp/dashboard-templates/service_system_metrics_dashboard.json.template > /etc/grafana/provisioning/dashboards/lastfm_etl_rest_api_system_metrics_dashboard.json
    echo "LastFM ETL REST API system metrics dashboard generated successfully"
    
    # LastFM Calls Generator
    export SERVICE_ID=9
    export SERVICE_JOB="lastfm-calls-generator"
    export SERVICE_TITLE="LastFM Calls Generator System Metrics"
    export SERVICE_UID="lastfm-calls-generator-system-metrics"
    envsubst < /tmp/dashboard-templates/service_system_metrics_dashboard.json.template > /etc/grafana/provisioning/dashboards/lastfm_calls_generator_system_metrics_dashboard.json
    echo "LastFM Calls Generator system metrics dashboard generated successfully"
    
    # LastFM Calls Performer
    export SERVICE_ID=10
    export SERVICE_JOB="lastfm-calls-performer"
    export SERVICE_TITLE="LastFM Calls Performer System Metrics"
    export SERVICE_UID="lastfm-calls-performer-system-metrics"
    envsubst < /tmp/dashboard-templates/service_system_metrics_dashboard.json.template > /etc/grafana/provisioning/dashboards/lastfm_calls_performer_system_metrics_dashboard.json
    echo "LastFM Calls Performer system metrics dashboard generated successfully"
    
    # LastFM Response Parser
    export SERVICE_ID=11
    export SERVICE_JOB="lastfm-response-parser"
    export SERVICE_TITLE="LastFM Response Parser System Metrics"
    export SERVICE_UID="lastfm-response-parser-system-metrics"
    envsubst < /tmp/dashboard-templates/service_system_metrics_dashboard.json.template > /etc/grafana/provisioning/dashboards/lastfm_response_parser_system_metrics_dashboard.json
    echo "LastFM Response Parser system metrics dashboard generated successfully"
else
    echo "WARNING: service_system_metrics_dashboard.json.template not found"
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
