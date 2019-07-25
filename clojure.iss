; Script generated by the Inno Script Studio Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define MyAppName "Clojure"
#define MyAppVersion "0.0.4"
#define MyAppPublisher "Rich Hickey"
#define MyAppURL "https://clojure.org/"
#define MyAppExeName "clojure.exe"
#define MyAppExeName2 "clj.exe"
#define ExeName "Clojure"
#define ExeName2 "Clj"
[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{519A0293-D0AC-45C8-9BA8-FB44805013BE}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={autopf64}\{#MyAppName}
DisableDirPage=yes
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
LicenseFile=epl-v10.txt
OutputDir=out
#ifdef CLAppVersion
OutputBaseFilename=clojure-install-{#CLAppVersion}
#endif
#ifndef CLAppVersion
OutputBaseFilename=clojure-install-{#MyAppVersion}
#endif
SetupIconFile=icon\install.ico
Compression=lzma
SolidCompression=yes
ChangesEnvironment=yes
UninstallDisplayIcon={app}\clojure-logo-icon-32.ico
[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
[Files]
Source: "ClojureTools\*"; DestDir: "{app}\Modules\ClojureTools"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "Files\*"; DestDir: "{app}\bin"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "InstallScripts\*"; DestDir: "{app}\InstallScripts"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "icon\clojure-logo-icon-32.ico"; DestDir: "{app}"; Flags: ignoreversion
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\{#ExeName}"; Filename: "{app}\bin\{#MyAppExeName}"; IconFilename: "{app}\clojure-logo-icon-32.ico"
Name: "{group}\{#ExeName2}"; Filename: "{app}\bin\{#MyAppExeName2}"; IconFilename: "{app}\clojure-logo-icon-32.ico"
Name: "{group}\{cm:ProgramOnTheWeb,{#MyAppName}}"; Filename: "{#MyAppURL}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"

[Dirs]
Name: "{app}\Modules\ClojureTools"
Name: "{app}\bin"

[Run]
Filename: "powershell"; Parameters: "-NoLogo -NonInteractive -NoProfile -InputFormat None -ExecutionPolicy Bypass -File ""{app}\InstallScripts\Path.ps1"" -path ""{app}\Modules"" -action remove -var PSModulePath"; Flags: runhidden
Filename: "powershell"; Parameters: "-NoLogo -NonInteractive -NoProfile -InputFormat None -ExecutionPolicy Bypass -File ""{app}\InstallScripts\Path.ps1"" -path ""{app}\Modules"" -action add -var PSModulePath"; Flags: runhidden
Filename: "powershell"; Parameters: "-NoLogo -NonInteractive -NoProfile -InputFormat None -ExecutionPolicy Bypass -File ""{app}\InstallScripts\Path.ps1"" -path ""{app}\bin"" -action remove -var Path"; Flags: runhidden
Filename: "powershell"; Parameters: "-NoLogo -NonInteractive -NoProfile -InputFormat None -ExecutionPolicy Bypass -File ""{app}\InstallScripts\Path.ps1"" -path ""{app}\bin"" -action add -var Path"; Flags: runhidden

[Code]
function InitializeSetup(): boolean;
var
  ResultCode: integer;
begin
  if Exec('java', '-version', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then begin
    Result := true;    
  end
  else begin          
    Result := false;
    if MsgBox('Clojure requires Java to run. Please download and install it, ensure it is in the path, and run this setup again. Do you want to download it now?', mbConfirmation, MB_YESNO) = idYes then begin
      ShellExec('open', 'https://jdk.java.net', '', '', SW_SHOWNORMAL, ewNoWait, ResultCode);
    end;  
  end;
end;

function PrepareToInstall(var NeedsRestart:Boolean): string;
var
  ResultCode: integer;
begin
  RegWriteStringValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\Policies\Microsoft\Windows\PowerShell', 'ExecutionPolicy', 'RemoteSigned');
  RegWriteDWordValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\Policies\Microsoft\Windows\PowerShell','EnableScripts', 1);
  // Filename: "powershell"; Parameters: "-NoLogo -NonInteractive -InputFormat None -NoProfile -ExecutionPolicy Bypass -File ""{tmp}\DeleteClojureTools.ps1"""; Flags: runhidden
  ExtractTemporaryFile('DeleteClojureTools.ps1');
  Exec('powershell', 
    ExpandConstant('-NoLogo -NonInteractive -InputFormat None -NoProfile -ExecutionPolicy Bypass -File "{tmp}\DeleteClojureTools.ps1"'),
    '', SW_HIDE , ewWaitUntilTerminated, ResultCode);
  // Proceed with Setup
  Result := '';
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
var ResultCode:integer;
begin
  if CurUninstallStep = usUninstall then 
  begin
      Exec('powershell',
        ExpandConstant('-NoLogo -NonInteractive -InputFormat None -NoProfile -ExecutionPolicy Bypass -File "{app}\InstallScripts\Path.ps1" -path "{app}\Modules" -action remove -var PSModulePath'), '', SW_HIDE ,
        ewWaitUntilTerminated, ResultCode);
      Exec('powershell',
        ExpandConstant('-NoLogo -NonInteractive -InputFormat None -NoProfile -ExecutionPolicy Bypass -File "{app}\InstallScripts\Path.ps1" -path "{app}\bin" -action remove -var Path'), '', SW_HIDE ,
        ewWaitUntilTerminated, ResultCode);
  end;
end;