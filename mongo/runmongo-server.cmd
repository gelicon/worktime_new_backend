@set MONGO-HOME=F:\MongoDB\mongodb-4.4.6
@set AUDIT-DB=.\data-audit

@if not exist %AUDIT-DB% mkdir %AUDIT-DB%
start %MONGO-HOME%\bin\mongod.exe --dbpath "%AUDIT-DB%" -f ".\mongod.cfg"