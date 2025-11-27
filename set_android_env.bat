@echo off
echo Setting up Android Environment Variables...

REM Set ANDROID_HOME
setx ANDROID_HOME "%LOCALAPPDATA%\Android\Sdk"
echo ANDROID_HOME set to: %LOCALAPPDATA%\Android\Sdk

REM Add to PATH
setx PATH "%PATH%;%LOCALAPPDATA%\Android\Sdk\platform-tools;%LOCALAPPDATA%\Android\Sdk\emulator;%LOCALAPPDATA%\Android\Sdk\tools;%LOCALAPPDATA%\Android\Sdk\tools\bin"
echo Added Android SDK paths to PATH

echo.
echo ========================================
echo Environment variables have been set!
echo ========================================
echo.
echo IMPORTANT: You MUST restart your terminal/PowerShell/CMD
echo for these changes to take effect!
echo.
echo After restarting, verify by running:
echo   echo %%ANDROID_HOME%%
echo   adb version
echo.
pause
