@echo off

:: Call java jar file to make dimens.xml files.
@java -jar %~dp0\screenMatchDP.jar 360

@rem "If you are not want to make some dimens.xml files below: "
@echo Be deleting files of 1365dp ...
@rmdir /q /s .\res\values-w1365dp
@echo Delete complete!

pause

