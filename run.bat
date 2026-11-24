@echo off
cd /d "%~dp0"

cd backend
start cmd /k mvnw.cmd spring-boot:run
cd ..\frontend
where bun >nul 2>nul
if errorlevel 1 (set CMD=npm) else (set CMD=bun)
start cmd /k %CMD% run dev
