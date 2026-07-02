$raw = [Console]::In.ReadToEnd()
try {
    $data = $raw | ConvertFrom-Json
    $path = $data.tool_input.file_path
    if ($null -eq $path) { exit 0 }

    $isApi = $path -match '[Cc]ontroller' -or $path -match '[\\\/]dto[\\\/]' -or $path -match 'Dto\.kt$'
    if ($isApi) {
        $date = Get-Date -Format 'yyyy-MM-dd'
        Write-Output ""
        Write-Output "==[ API SYNC CHECK ]============================================"
        Write-Output "  Fayl o'zgardi : $([System.IO.Path]::GetFileName($path))"
        Write-Output "  → docs/API.md yangilandi? (endpoint shape o'zgardimi?)"
        Write-Output "  → Changelog : file-master-front/docs/api/$date-<slug>.md"
        Write-Output "================================================================"
    }
} catch { exit 0 }
