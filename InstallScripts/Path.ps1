param([String]$path="C:\Program Files\blahblah",[String]$action="add",[String]$var="Path")
function NormalizePath {
    param([string]$p)
    $p = $p.ToLower()
    If ($p.Substring($p.Length-1, 1) -eq "\") {
      $p.Substring(0, $p.Length-1)
    } Else {
      $p
    }   
  }
function AddPath {    
    param([String]$path,[String]$var)
    $p = [Environment]::GetEnvironmentVariable($var, "Machine")
    $p += ";" + $path
    Write-Host $p
    [Environment]::SetEnvironmentVariable($var,$p, "Machine")
}

function RemovePath {
    param([String]$path,[String]$var)
    $normalizedPath = NormalizePath($path)
    $Paths = [Environment]::GetEnvironmentVariable($var,"Machine")
    $Paths =  $Paths -split ";"
    $Paths =  $Paths | Select-Object -unique 
    $list = @()
    foreach ($item in $Paths) {
      $normalizedItem = NormalizePath($item)
      if ($normalizedItem -ne $normalizedPath) {
        $list += $item
      }
    }
    $list=$list -join ";"
    Write-Host $list
    [Environment]::SetEnvironmentVariable($var,$list, "Machine")
}

if ($action -eq "add") {
    AddPath -var $var -path $path
} elseif ($action -eq "remove") {
    RemovePath -var $var -path $path
}
