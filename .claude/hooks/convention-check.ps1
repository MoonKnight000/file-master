$raw = [Console]::In.ReadToEnd()
try {
    $data = $raw | ConvertFrom-Json
    $path = $data.tool_input.file_path
    if ($null -eq $path -or -not ($path -match '\.kt$')) { exit 0 }

    $filename = [System.IO.Path]::GetFileNameWithoutExtension($path)
    $dir      = [System.IO.Path]::GetDirectoryName($path)
    $warnings = @()

    # Impl fayllar uchun — tegishli interface bor-yo'qligini tekshir
    if ($filename -match '(Controller|Service|Worker)Impl$') {
        $iface = $filename -replace 'Impl$', ''
        $ifacePath = Join-Path $dir "$iface.kt"
        if (-not (Test-Path $ifacePath)) {
            $warnings += "Interface topilmadi: $iface.kt ($dir ichida bo'lishi shart)"
        }
    }

    # ResponseEntity<ResponseData<*>> qaytaryaptimi (Controller Impl uchun)
    if ($filename -match 'ControllerImpl$') {
        $content = $data.tool_input.content
        if ($null -ne $content -and -not ($content -match 'ResponseEntity')) {
            $warnings += "Controller metodlari ResponseEntity<ResponseData<T>> qaytarishi shart"
        }
    }

    # One class per file
    if ($null -ne $data.tool_input.content) {
        $content = $data.tool_input.content
        $classCount = ([regex]::Matches($content, '(?m)^(class|interface|object|data class|enum class|abstract class)\s')).Count
        if ($classCount -gt 1) {
            $warnings += "Bir faylda $classCount top-level ta'rif bor — har bir class alohida faylda bo'lishi shart"
        }
    }

    if ($warnings.Count -gt 0) {
        Write-Output ""
        Write-Output "==[ CONVENTION CHECK ]=========================================="
        foreach ($w in $warnings) {
            Write-Output "  WARN  $w"
        }
        Write-Output "================================================================"
    }
} catch { exit 0 }
