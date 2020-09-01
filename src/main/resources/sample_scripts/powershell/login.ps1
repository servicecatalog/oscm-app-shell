<#This is authentication script which must be included at the begining of every other powershell script interacting with Microsoft Azure#>

Function Check-Token
{
    Param($AzureContext)
    $cacheItems = $azureContext.TokenCache.ReadItems()
    $tokenExpirationDate = $cacheItems.ExpiresOn.DateTime
    $tokenExpired = $tokenExpirationDate -lt (Get-Date)
    return $tokenExpired
}

Function Check-AzureParam
{
    Param($AzureParameter, $AzureParameterName)
    if ($AzureParameter -eq $null)
    {
        throw "Custom attribute $AzureParameterName is not defined"
    }
}

$ErrorActionPreference = 'Stop'
$loginError = $false

Try
{
    Check-AzureParam -AzureParameter $AzureApplicationId -AzureParameterName "AzureApplicationId"
    Check-AzureParam -AzureParameter $AzureTenantId -AzureParameterName "AzureTenantId"
    Check-AzureParam -AzureParameter $AzureSubscriptionId -AzureParameterName "AzureSubscriptionId"
    Check-AzureParam -AzureParameter $AzureSecret -AzureParameterName "AzureSecret"

    $azureContext = Get-AzContext

    if ($azureContext -eq $null -Or (Check-Token -AzureContext $azureContext))
    {
        $password = ConvertTo-SecureString -String $AzureSecret -AsPlainText
        $credential = New-Object -TypeName System.Management.Automation.PSCredential -ArgumentList $AzureApplicationId, $password
        $connection = Connect-AzAccount -Credential $credential -Tenant $AzureTenantId -ServicePrincipal -WarningAction Ignore
        $azureContext = Get-AzContext
    }

    if ($azureContext.Subscription.Id -ne $AzureSubscriptionId)
    {
        $logout = Logout-AzAccount
        throw "AzureSubscriptionId is invalid. Please check custom attribute value"
    }

    if ($azureContext.Tenant.Id -ne $AzureTenantId)
    {
        $logout = Logout-AzAccount
        throw "AzureTenantId is invalid. Please check custom attribute value"
    }
}
Catch
{
    $loginError = $true
    $errorCommand = $PSItem.InvocationInfo.MyCommand
    $errorMsg = $_.Exception.Message -replace "'", ""
    Write-Output "{'status':'error','message':'$( $errorCommand ): $( $errorMsg )'}"
    Write-Output "END_OF_SCRIPT"
}