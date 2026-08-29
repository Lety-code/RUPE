@echo off
title RUPE 100 - Compilar y ejecutar
echo ============================================
echo Compilando sistema RUPE 100%%...
echo ============================================
call mvn clean package -DskipTests
if errorlevel 1 (
    echo.
    echo ERROR: No se pudo compilar el proyecto.
    pause
    exit /b 1
)
echo.
echo ============================================
echo Ejecutando RUPE en modo local con H2...
echo URL: http://localhost:8080
echo Usuario: usadminrupe@rupe.com
echo Contrasena: rupe987
echo ============================================
java -jar target\rupe-100-completo.jar --spring.profiles.active=local
pause
