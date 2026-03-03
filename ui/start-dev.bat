@echo off
REM Windows batch script to start UI in development mode

REM Load environment variables from .env file
for /f "tokens=*" %%a in (.env) do (
  set "%%a"
)

REM Start development server
npm run dev
