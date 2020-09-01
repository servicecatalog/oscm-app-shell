<#Simple script for listing all automation accounts within given context#>

. /opt/scripts/login.ps1
if ($loginError -eq $true)
{
    Exit 1
}

Try
{
    $accounts = Get-AzAutomationAccount
    $accountsDetails = $accounts | ForEach-Object { Write-Output  "<p><span style=\'margin-left:30px;color:blue;width:400px\'>$( $_.AutomationAccountName )</span> created at: $( $_.CreationTime )<br></p>" }

    Write-Output "{'status':'ok','message':'Script execution is successful', 'data': {'output':
        '<b>Azure subscription name:</b> $( $azureContext.Subscription.Name ) <br>
         <b>Azure subscription id:</b> $( $azureContext.Subscription.Id ) <br>
         <b>Azure tenant id:</b> $( $azureContext.Tenant.Id ) <br><br>
         <b>Available automation accounts:</b> $( $accountsDetails )'}}"

    Write-Output "END_OF_SCRIPT"
}
Catch
{
    $errorCommand = $PSItem.InvocationInfo.MyCommand
    $errorMsg = $_.Exception.Message -replace "'", ""
    Write-Output "{'status':'error','message':'$( $errorCommand ): $( $errorMsg )'}"
    Write-Output "END_OF_SCRIPT"
}