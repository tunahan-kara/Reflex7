param(
    [string]$Source = (Join-Path $PSScriptRoot "..\release-assets\reflex7-icon-512.png")
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$androidRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$resRoot = Join-Path $androidRoot "app\src\main\res"
$sourceBitmap = [System.Drawing.Bitmap]::FromFile((Resolve-Path $Source))

function New-Canvas([int]$width, [int]$height) {
    return [System.Drawing.Bitmap]::new($width, $height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
}

function Save-Png($bitmap, [string]$path) {
    [System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($path)) | Out-Null
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
}

function Draw-Scaled($source, $destination, [System.Drawing.Rectangle]$bounds) {
    $graphics = [System.Drawing.Graphics]::FromImage($destination)
    try {
        $graphics.Clear([System.Drawing.Color]::Transparent)
        $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
        $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $graphics.DrawImage($source, $bounds, 0, 0, $source.Width, $source.Height, [System.Drawing.GraphicsUnit]::Pixel)
    } finally {
        $graphics.Dispose()
    }
}

function New-SymbolLayer([bool]$monochrome) {
    $isolated = New-Canvas 512 512
    for ($y = 0; $y -lt 512; $y++) {
        for ($x = 0; $x -lt 512; $x++) {
            $pixel = $sourceBitmap.GetPixel($x, $y)
            $greenSignal = $pixel.G - [Math]::Max($pixel.R, $pixel.B)
            if ($monochrome) {
                $alpha = [Math]::Clamp(($greenSignal - 12) * 10, 0, 255)
                if ($alpha -gt 0) {
                    $isolated.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($alpha, 255, 255, 255))
                }
            } else {
                $alpha = [Math]::Clamp(($greenSignal - 2) * 4, 0, 255)
                if ($alpha -gt 0) {
                    $isolated.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($alpha, $pixel.R, $pixel.G, $pixel.B))
                }
            }
        }
    }

    # The original symbol spans roughly 65% of the source. A subtle 92% reduction
    # keeps it inside Android's strict 66x66dp adaptive safe zone on every mask.
    $safeLayer = New-Canvas 512 512
    Draw-Scaled $isolated $safeLayer ([System.Drawing.Rectangle]::new(20, 20, 472, 472))
    $isolated.Dispose()
    return $safeLayer
}

function New-LegacyIcon([int]$size, [bool]$round) {
    $icon = New-Canvas $size $size
    $graphics = [System.Drawing.Graphics]::FromImage($icon)
    try {
        $graphics.Clear([System.Drawing.Color]::Transparent)
        $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $graphics.DrawImage($sourceBitmap, 0, 0, $size, $size)
    } finally {
        $graphics.Dispose()
    }
    if ($round) {
        $center = ($size - 1) / 2.0
        $radiusSquared = ($size / 2.0) * ($size / 2.0)
        for ($y = 0; $y -lt $size; $y++) {
            for ($x = 0; $x -lt $size; $x++) {
                $dx = $x - $center
                $dy = $y - $center
                if (($dx * $dx + $dy * $dy) -gt $radiusSquared) {
                    $icon.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
                }
            }
        }
    } else {
        $radius = [Math]::Max(4, [int]($size * 0.18))
        $edge = $size - 1
        for ($y = 0; $y -lt $size; $y++) {
            for ($x = 0; $x -lt $size; $x++) {
                $nearestX = [Math]::Clamp($x, $radius, $edge - $radius)
                $nearestY = [Math]::Clamp($y, $radius, $edge - $radius)
                $dx = $x - $nearestX
                $dy = $y - $nearestY
                if (($dx * $dx + $dy * $dy) -gt ($radius * $radius)) {
                    $icon.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
                }
            }
        }
    }
    return $icon
}

try {
    $foreground = New-SymbolLayer $false
    $monochrome = New-SymbolLayer $true
    try {
        Save-Png $foreground (Join-Path $resRoot "drawable-nodpi\ic_launcher_foreground.png")
        Save-Png $monochrome (Join-Path $resRoot "drawable-nodpi\ic_launcher_monochrome.png")
    } finally {
        $foreground.Dispose()
        $monochrome.Dispose()
    }

    $densities = [ordered]@{
        "mdpi" = 48
        "hdpi" = 72
        "xhdpi" = 96
        "xxhdpi" = 144
        "xxxhdpi" = 192
    }
    foreach ($entry in $densities.GetEnumerator()) {
        $directory = Join-Path $resRoot "mipmap-$($entry.Key)"
        $legacy = New-LegacyIcon $entry.Value $false
        $round = New-LegacyIcon $entry.Value $true
        try {
            Save-Png $legacy (Join-Path $directory "ic_launcher.png")
            Save-Png $round (Join-Path $directory "ic_launcher_round.png")
        } finally {
            $legacy.Dispose()
            $round.Dispose()
        }
    }
} finally {
    $sourceBitmap.Dispose()
}

Write-Output "Generated Reflex7 launcher resources from $Source"
