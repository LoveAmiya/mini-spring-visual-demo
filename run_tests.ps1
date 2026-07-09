$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

Push-Location "mini-spring-core"
mvn clean install
Pop-Location

mvn test
