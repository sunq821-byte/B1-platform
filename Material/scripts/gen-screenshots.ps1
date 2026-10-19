Add-Type -AssemblyName System.Drawing

function Save-MockScreenshot {
    param(
        [string]$Path,
        [string]$Title,
        [string]$Subtitle,
        [ValidateSet("png", "jpg")]
        [string]$Format
    )
    $w = 960
    $h = 540
    $bmp = New-Object System.Drawing.Bitmap $w, $h
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::FromArgb(245, 247, 250))
    $headerBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(22, 119, 255))
    $g.FillRectangle($headerBrush, 0, 0, $w, 72)
    $titleFont = New-Object System.Drawing.Font("Microsoft YaHei", 20, [System.Drawing.FontStyle]::Bold)
    $subFont = New-Object System.Drawing.Font("Microsoft YaHei", 13)
    $g.DrawString($Title, $titleFont, [System.Drawing.Brushes]::White, 24, 18)
    $g.DrawString($Subtitle, $subFont, [System.Drawing.Brushes]::DimGray, 24, 120)
    $g.DrawRectangle([System.Drawing.Pens]::LightGray, 24, 180, ($w - 48), ($h - 220))
    $g.DrawString("（演示截图 · 衡迹平台 Mock 素材）", $subFont, [System.Drawing.Brushes]::Gray, 24, ($h - 48))
    if ($Format -eq "png") {
        $bmp.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    } else {
        $codec = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() | Where-Object { $_.MimeType -eq "image/jpeg" }
        $encParams = New-Object System.Drawing.Imaging.EncoderParameters 1
        $encParams.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter ([System.Drawing.Imaging.Encoder]::Quality, 90)
        $g.Save($Path, $codec, $encParams)
    }
    $g.Dispose()
    $bmp.Dispose()
}

$base = Join-Path $PSScriptRoot "..\04-界面截图"
Save-MockScreenshot (Join-Path $base "book-mgr-ui.png") "图书管理系统" "借阅列表 · 运行截图" "png"
Save-MockScreenshot (Join-Path $base "redis-cache-screenshot.png") "Redis 缓存压测" "QPS 4200 · 命中率 96%" "png"
Save-MockScreenshot (Join-Path $base "vue-component-preview.jpg") "Vue3 组件库预览" "BaseButton / BaseInput 组件演示" "jpg"
Save-MockScreenshot (Join-Path $base "servlet-session-run.jpg") "Servlet 会话管理" "登录后会话写入 Session 演示" "jpg"
Write-Host "screenshots generated"
