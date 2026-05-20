$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..

$sources = @(Get-ChildItem -Recurse -Filter *.java -Path main).FullName
if ($sources.Count -eq 0) {
  Write-Error "No .java files found under main/"
}

javac -encoding UTF-8 -cp "main;lib/sqlite-jdbc.jar" @sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

java --enable-native-access=ALL-UNNAMED -cp "main;lib/sqlite-jdbc.jar" masroofy.Main
