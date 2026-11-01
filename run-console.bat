@echo off
title ShopEase E-Commerce System (Interactive Console Mode)
echo ======================================================================
echo     Starting ShopEase E-Commerce Console System (PDF Menu Mode)       
echo ======================================================================
echo.
echo Launching Interactive Console Menu with while-loop and switch statement...
echo.

if exist mvnw.cmd (
    call .\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--console"
) else (
    call mvn spring-boot:run -Dspring-boot.run.arguments="--console"
)

pause
