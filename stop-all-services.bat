@echo off
REM Batch script to stop all CyberLearnix LMS microservices

echo 🛑 Stopping CyberLearnix LMS Microservices...
echo.

REM Define ports used by microservices
set "PORTS=8761 8888 8080 8081 8082 8083 8084 8085 8086"

for %%p in (%PORTS%) do (
    echo 🔍 Checking port %%p...
    
    REM Find processes using the port
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%%p') do (
        set PID=%%a
        REM Check if it's a Java process
        for /f "tokens=1" %%b in ('tasklist /FI "PID eq %%a" /NH 2^>nul ^| findstr java.exe') do (
            if "%%b"=="java.exe" (
                echo    📍 Found Java process PID %%a on port %%p
                echo    🛑 Stopping process %%a...
                taskkill /PID %%a /F >nul 2>&1
                if !errorlevel! == 0 (
                    echo    ✅ Successfully stopped process %%a
                ) else (
                    echo    ❌ Failed to stop process %%a
                )
            )
        )
    )
)

echo.
echo ✅ All microservices stopped!
echo 💡 You can now start your services safely!
echo.

REM Show final port status
echo 🔍 Final port check:
for %%p in (%PORTS%) do (
    netstat -ano | findstr :%%p >nul 2>&1
    if !errorlevel! == 0 (
        echo    ❌ Port %%p still in use
    ) else (
        echo    ✅ Port %%p is free
    )
)

pause