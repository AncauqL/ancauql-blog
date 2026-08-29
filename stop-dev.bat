@echo off
setlocal
chcp 936 >nul
title AncauqL Blog 停止器

echo 正在检查端口 9999（后端 java）和 8080/8081（前端 node）...
powershell -NoProfile -Command "$map = @{9999='java'; 8080='node'; 8081='node'}; $found = $false; foreach ($port in $map.Keys) { Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object { $proc = Get-Process -Id $_ -ErrorAction SilentlyContinue; if ($proc -and $proc.ProcessName -eq $map[$port]) { $found = $true; Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue; Start-Sleep -Milliseconds 300; if (Get-Process -Id $_ -ErrorAction SilentlyContinue) { Write-Host ('  [失败] 端口 ' + $port + ' 的 ' + $map[$port] + ' (PID ' + $_ + ') 无法停止，可能需要管理员权限') } else { Write-Host ('  [停止] 端口 ' + $port + ' 的 ' + $map[$port] + ' (PID ' + $_ + ')') } } elseif ($proc) { Write-Host ('  [跳过] 端口 ' + $port + ' 被 ' + $proc.ProcessName + ' 占用，不是本项目进程，不动它') } } }; if (-not $found) { Write-Host '  没有发现运行中的博客服务' }"
echo.
echo 完成。残留的命令行窗口可以直接关闭。
pause
