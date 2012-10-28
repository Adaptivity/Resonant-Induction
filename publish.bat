::ASSEMBLY LINE BUILDER
@echo off
echo Promotion Type?
set /p PROMOTION=

set /p MODVERSION=<modversion.txt
set /p CurrentBuild=<buildnumber.txt
set /a BUILD_NUMBER=%CurrentBuild%+1
echo %BUILD_NUMBER% >buildnumber.txt

set FILE_NAME=AssemblyLine_v%MODVERSION%.%BUILD_NUMBER%.jar
set API_NAME=AssemblyLine_v%MODVERSION%.%BUILD_NUMBER%_api.zip
set BACKUP_NAME=AssemblyLine_v%MODVERSION%.%BUILD_NUMBER%_backup.zip

echo Starting to build %FILE_NAME%

::BUILD
runtime\bin\python\python_mcp runtime\recompile.py %*
runtime\bin\python\python_mcp runtime\reobfuscate.py %*

::ZIP-UP
cd reobf\minecraft\
"..\..\..\7za.exe" a "..\..\builds\%FILE_NAME%" "*"

cd ..\..\
"..\7za.exe" a "builds\%FILE_NAME%" "mcmod.info"

cd resources\
"..\..\7za.exe" a "..\builds\%FILE_NAME%" "*"
"..\..\7za.exe" a "..\builds\%BACKUP_NAME%" "*" -pcalclavia
cd ..\
cd src\
"..\..\7za.exe" a "..\builds\%BACKUP_NAME%" "*\assemblyline\" -pcalclavia
::"..\..\7za.exe" a "..\builds\%API_NAME%" "*\atomicscience\api\"
cd ..\

::UPDATE INFO FILE %API_NAME%
echo %PROMOTION% %FILE_NAME%>>info.txt

::GENERATE FTP Script
echo open www.calclavia.com>ftpscript.txt
echo al@calclavia.com>>ftpscript.txt
echo f@Gwk%qFJ4kc>>ftpscript.txt
echo binary>>ftpscript.txt
echo put "builds\%FILE_NAME%">>ftpscript.txt
::echo put "builds\%API_NAME%">>ftpscript.txt
echo put info.txt>>ftpscript.txt
echo quit>>ftpscript.txt
ftp.exe -s:ftpscript.txt
del ftpscript.txt

echo Done building %FILE_NAME% for UE %UE_VERSION%

pause