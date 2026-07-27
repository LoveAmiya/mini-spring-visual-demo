$ErrorActionPreference = "Stop"

Push-Location (Join-Path $PSScriptRoot "mini-spring-core")
try {
    mvn.cmd test
    if ($LASTEXITCODE -ne 0) {
        throw "Mini-Spring core tests failed with exit code $LASTEXITCODE"
    }
}
finally {
    Pop-Location
}

Push-Location $PSScriptRoot
try {
    mvn.cmd test
    if ($LASTEXITCODE -ne 0) {
        throw "Mini-Spring web tests failed with exit code $LASTEXITCODE"
    }
}
finally {
    Pop-Location
}
