# --- CONFIGURATION ---
$ApiKey = "mysecretapikey"
$SyncthingBaseUrl = "http://localhost:8384"

function Get-NodeID {
    [CmdletBinding()]
    param (
        [Parameter(Mandatory = $true)][string]$Ip,
        [Parameter(Mandatory = $true)][string]$ApiKey
    )
    
    try {
        $response = Invoke-RestMethod -Uri "http://${Ip}:8384/rest/system/status" -Headers @{ "X-API-Key" = $ApiKey } -Method Get -ErrorAction Stop
        return $response.myID
    }
    catch {
        Write-Error "Failed to get Node ID from $Ip : $_"
        return $null
    }
}

function Add-Device {
    [CmdletBinding()]
    param (
        [Parameter(Mandatory = $true)][string]$Ip,
        [Parameter(Mandatory = $true)][string]$NodeID,
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][string]$ApiKey,
        [Parameter(Mandatory = $true)][string]$BaseUrl
    )

    $Headers = @{
        "X-API-Key" = $ApiKey
    }
    $devicePayload = @{
        deviceID  = $NodeID
        name      = $Name
        addresses = @("tcp://${Ip}:22000")
    }
    $JsonDevice = $devicePayload | ConvertTo-Json -Depth 4
    try {
        Invoke-RestMethod -Uri "$BaseUrl/rest/config/devices" `
            -Method Post `
            -Headers $Headers `
            -Body $JsonDevice `
            -ContentType "application/json"
        Write-Host "Device '$Name' added successfully." -ForegroundColor Green
    }
    catch {
        Write-Host "Error adding device '$Name': $_" -ForegroundColor Red
    }
}

function Add-Folder {
    [CmdletBinding()]
    param (
        [Parameter(Mandatory = $true)][string]$Id,
        [Parameter(Mandatory = $true)][string]$Label,
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Type,
        [Parameter(Mandatory = $true)][array]$Devices,
        [Parameter(Mandatory = $true)] [string]$ApiKey,
        [Parameter(Mandatory = $true)][string]$BaseUrl
    )

    $Headers = @{
        "X-API-Key" = $ApiKey
    }
    $folderPayload = @{
        id      = $Id
        label   = $Label
        path    = $Path
        type    = $Type
        devices = $Devices
    }
    $JsonFolder = $folderPayload | ConvertTo-Json -Depth 4
    try {
        Invoke-RestMethod -Uri "$BaseUrl/rest/config/folders" `
            -Method Post `
            -Headers $Headers `
            -Body $JsonFolder `
            -ContentType "application/json"
        Write-Host "Folder '$Label' added successfully." -ForegroundColor Green
    }
    catch {
        Write-Host "Error adding folder '$Label': $_" -ForegroundColor Red
    }
}

Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process


# Remote Node Details (Replace these with the actual IDs you captured)
$IpNode2 = "192.168.1.135"
$IdNode2 = Get-NodeID -Ip $IpNode2 -ApiKey $ApiKey

$IpNode3 = "192.168.1.143"
$IdNode3 = Get-NodeID -Ip $IpNode3 -ApiKey $ApiKey

# --- STEP 1: ADD REMOTE DEVICES ---
Write-Host ">>> 1. Adding Remote Devices (Node 2 and Node 3)..." -ForegroundColor Cyan

if ($IdNode2) {
    Add-Device -Ip $IpNode2 -NodeID $IdNode2 -Name "Node 2" -ApiKey $ApiKey -BaseUrl $SyncthingBaseUrl
}
if ($IdNode3) {
    Add-Device -Ip $IpNode3 -NodeID $IdNode3 -Name "Node 3" -ApiKey $ApiKey -BaseUrl $SyncthingBaseUrl
}

# --- STEP 2: ADD THE DATALAKE FOLDER ---
Write-Host "`n>>> 2. Adding the Datalake Folder..." -ForegroundColor Cyan

$devicesList = @()
if ($IdNode2) { $devicesList += @{ deviceID = $IdNode2 } }
if ($IdNode3) { $devicesList += @{ deviceID = $IdNode3 } }

Add-Folder -Id "datalake-id" -Label "Engine Datalake" -Path "/app/datalake" -Type "sendreceive" -Devices $devicesList -ApiKey $ApiKey -BaseUrl $SyncthingBaseUrl

# --- STEP 3: RESTART SYNCTHING ---
Write-Host "`n>>> 3. Restarting Syncthing to apply changes..." -ForegroundColor Cyan

$GlobalHeaders = @{
    "X-API-Key" = $ApiKey
}

try {
    Invoke-RestMethod -Uri "$SyncthingBaseUrl/rest/system/restart" `
        -Method Post `
        -Headers $GlobalHeaders
    Write-Host "Restart command sent. Please wait a few seconds for the nodes to connect." -ForegroundColor Green
}
catch {
    Write-Host "Error restarting service: $_" -ForegroundColor Red
}