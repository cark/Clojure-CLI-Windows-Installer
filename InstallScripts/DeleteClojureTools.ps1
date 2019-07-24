#Echo yoh > C:\#trans\a.txt
function Delete-ClojureTools {
    $Command = Get-Command Invoke-Clojure -ErrorAction SilentlyContinue
    #Echo $Command >> C:\#trans\a.txt
    If ($Command) {
        $Module = $Command.Module
        Remove-Module $Module.Name
        Remove-Item $Module.ModuleBase -Recurse -Force
        Delete-ClojureTools
    }
}
Delete-ClojureTools

