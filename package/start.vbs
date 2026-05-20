Set objShell = CreateObject("WScript.Shell")
Set objFSO = CreateObject("Scripting.FileSystemObject")

scriptDir = objFSO.GetParentFolderName(WScript.ScriptFullName)

' Find Java - try JDK 17 first, then JAVA_HOME, then PATH
javaPath = "C:\Program Files\Microsoft\jdk-17.0.12.7-hotspot\bin\javaw.exe"
If Not objFSO.FileExists(javaPath) Then
    javaHome = objShell.ExpandEnvironmentStrings("%JAVA_HOME%")
    If javaHome <> "%JAVA_HOME%" Then
        javaPath = javaHome & "\bin\javaw.exe"
    End If
End If
If Not objFSO.FileExists(javaPath) Then javaPath = "javaw.exe"

' Find JAR
jarPath = scriptDir & "\ClassNote.jar"
If Not objFSO.FileExists(jarPath) Then
    jarPath = scriptDir & "\..\target\classnote-local-1.0.0.jar"
End If

If Not objFSO.FileExists(jarPath) Then
    MsgBox "JAR not found! Run package.bat first.", 48, "ClassNote"
    WScript.Quit
End If

' Start Spring Boot (hidden window)
objShell.Run """" & javaPath & """ -jar """ & jarPath & """ --spring.profiles.active=h2", 0, False

' Wait a few seconds for server to start
WScript.Sleep 5000

' Try to open Chrome app mode
On Error Resume Next
objShell.Run "cmd /c start chrome --app=http://localhost:8080 --start-maximized", 0, False
If Err.Number <> 0 Then
    objShell.Run "cmd /c start msedge --app=http://localhost:8080 --start-maximized", 0, False
End If
On Error Goto 0
