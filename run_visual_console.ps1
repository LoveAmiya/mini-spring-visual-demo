$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

Push-Location "mini-spring-core"
mvn clean install
Pop-Location

Write-Host "Starting Mini-Spring Visual Console..."
Write-Host "Open http://127.0.0.1:18080"

mvn exec:java '-Dexec.mainClass=com.test.minispring.web.MiniSpringWebServer'
