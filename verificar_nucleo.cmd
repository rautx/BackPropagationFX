@echo off
setlocal
cd /d "%~dp0"
if not exist "verificacao-bin" mkdir "verificacao-bin"
set "ARQUIVO_LISTA=%TEMP%\backprop_fontes_%RANDOM%.txt"
for %%F in (src\main\java\com\example\backpropagationfx\modelo\*.java) do >>"%ARQUIVO_LISTA%" echo "%%~fF"
>>"%ARQUIVO_LISTA%" echo "%CD%\testes\VerificacaoSemJavaFX.java"
javac -encoding UTF-8 -d verificacao-bin @"%ARQUIVO_LISTA%"
if errorlevel 1 (
    echo Falha ao compilar o nucleo. Verifique a instalacao do JDK.
) else (
    java -cp verificacao-bin VerificacaoSemJavaFX
)
del "%ARQUIVO_LISTA%" >nul 2>nul
pause
