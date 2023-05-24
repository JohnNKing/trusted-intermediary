resource "azurerm_log_analytics_workspace" "logs_workspace" {
  name = "ti-logs-${var.environment}"

  resource_group_name = azurerm_resource_group.group.name
  location            = azurerm_resource_group.group.location
}

resource "azurerm_monitor_diagnostic_setting" "app_to_logs" {
  name                       = "ti-app-to-logs-${var.environment}"
  target_resource_id         = azurerm_linux_web_app.api.id
  log_analytics_workspace_id = azurerm_log_analytics_workspace.logs_workspace.id

  log_analytics_destination_type = "Dedicated"

  enabled_log {
    category = "AppServiceConsoleLogs"
  }
  enabled_log {
    category = "AppServiceAppLogs"
  }
  enabled_log {
    category = "AppServiceHTTPLogs"
  }
}