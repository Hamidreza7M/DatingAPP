@echo off
setlocal
cd /d "%~dp0"

echo ==========================================
echo          FastLove Admin - Demo
echo ==========================================

where node >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Node.js is not installed or is not in PATH.
  echo Install Node.js 20 or newer, then run this file again.
  pause
  exit /b 1
)

where npm >nul 2>nul
if errorlevel 1 (
  echo [ERROR] npm was not found. Reinstall Node.js 20 or newer.
  pause
  exit /b 1
)

node -e "if (Number(process.versions.node.split('.')[0]) < 20) process.exit(1)"
if errorlevel 1 (
  echo [ERROR] Node.js 20 or newer is required. Current version:
  node --version
  pause
  exit /b 1
)

echo.
echo Admin URL : http://localhost:4177
echo Username  : admin
echo Password  : 123456
echo.
echo Keep this window open while using the admin panel.
echo Press Ctrl+C to stop the server.
echo.
call npm run admin:dev

echo.
echo FastLove Admin server stopped.
pause
