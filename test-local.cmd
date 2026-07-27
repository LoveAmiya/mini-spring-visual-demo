@echo off
setlocal
pushd "%~dp0mini-spring-core"
call mvn.cmd test
if errorlevel 1 (
  popd
  exit /b 1
)
popd
pushd "%~dp0"
call mvn.cmd test
set "RESULT=%errorlevel%"
popd
exit /b %RESULT%
