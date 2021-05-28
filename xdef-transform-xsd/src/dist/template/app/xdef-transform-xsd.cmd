@echo off
@echo.
@echo Executing XDef2XmlSchema transformation

java -Dlogback.configurationFile=config/logback.xml -jar xdef-transform-xsd-${project.version}.jar %*