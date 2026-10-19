# 将 Material/manifest.json 中的文件手动上传至 MinIO（开发环境）
# 需先启动 docker compose 中的 MinIO（localhost:9000）

$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$manifestPath = Join-Path $root "manifest.json"

if (-not (Test-Path $manifestPath)) {
    Write-Error "manifest.json not found: $manifestPath"
}

$endpoint = $env:MINIO_ENDPOINT
if (-not $endpoint) { $endpoint = "http://localhost:9000" }
$accessKey = if ($env:MINIO_ACCESS_KEY) { $env:MINIO_ACCESS_KEY } else { "minioadmin" }
$secretKey = if ($env:MINIO_SECRET_KEY) { $env:MINIO_SECRET_KEY } else { "minioadmin" }

Write-Host "Material root: $root"
Write-Host "MinIO endpoint: $endpoint"
Write-Host ""
Write-Host "推荐方式：启动后端 dev 环境，MaterialSeedRunner 会自动同步。"
Write-Host "若需独立脚本，请安装 MinIO Client (mc) 后配置 alias："
Write-Host "  mc alias set local $endpoint $accessKey $secretKey"
Write-Host ""

$manifest = Get-Content $manifestPath -Raw | ConvertFrom-Json
foreach ($f in $manifest.files) {
    $local = Join-Path $root $f.localPath
    if (-not (Test-Path $local)) {
        Write-Warning "Missing: $local"
        continue
    }
    Write-Host "[OK] $($f.localPath) -> $($f.bucket)/$($f.objectKey)"
}

Write-Host ""
Write-Host "Done listing. Use 'mvn spring-boot:run' for automatic upload."
