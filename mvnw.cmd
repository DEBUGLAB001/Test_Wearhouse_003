@echo off
setlocal EnableDelayedExpansion

rem ShopSphere Pro Maven Wrapper for Windows

if not defined MAVEN_OPTS set MAVEN_OPTS=-Xmx512m -Xms64m

if "%1"=="test" (
    rem Execution engine test contract: compile and run ChallengeTestRunner with strictly JSON output
    call "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1.1.1\plugins\maven\lib\maven3\bin\mvn.cmd" test-compile -q
    java -Xmx256m -Xms64m -cp "target\classes;target\test-classes" com.shopsphere.ChallengeTestRunner
    exit /b !ERRORLEVEL!
)

call "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1.1.1\plugins\maven\lib\maven3\bin\mvn.cmd" %*
exit /b !ERRORLEVEL!
