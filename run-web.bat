@echo off
title ShopEase E-Commerce System (Web Mode)
echo ======================================================================
echo           Starting ShopEase E-Commerce Application (Web Mode)        
echo ======================================================================
echo.
echo Application URL: http://localhost:8080
echo Admin Portal:    http://localhost:8080/admin.html
echo Credentials:     admin@shop.com / admin123
echo                  user@shop.com  / user123
echo Database:        ecommerce_db (H2 local or MySQL)
echo H2 Console:      http://localhost:8080/h2-console
echo.
echo Press Ctrl+C at any time to stop the server.
echo ======================================================================

if exist mvnw.cmd (
    call .\mvnw.cmd spring-boot:run
) else (
    call mvn spring-boot:run
)

pause
