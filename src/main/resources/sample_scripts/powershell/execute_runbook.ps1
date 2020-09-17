<#Simple script for executing existing runbook within given context#>

. /opt/scripts/login.ps1
if ($loginError -eq $true)
{
    Exit 1
}

Try
{
    Check-ServiceAzureParam -AzureParameter $AzureAutomationAccountName -AzureParameterName "AzureAutomationAccountName"
    Check-ServiceAzureParam -AzureParameter $AzureResourceGroupName -AzureParameterName "AzureResourceGroupName"
    Check-ServiceAzureParam -AzureParameter $AzureRunbookName -AzureParameterName "AzureRunbookName"

    $runbook_exec = Start-AzAutomationRunbook -Name $AzureRunbookName -ResourceGroupName $AzureResourceGroupName -AutomationAccountName $AzureAutomationAccountName

    Write-Output "{'status':'ok','message':'Script execution is successful'}"
    Write-Output "END_OF_SCRIPT"
}
Catch
{
    $errorCommand = $PSItem.InvocationInfo.MyCommand
    $errorMsg = $_.Exception.Message -replace "'", ""
    Write-Output "{'status':'error','message':'$( $errorCommand ): $( $errorMsg )'}"
    Write-Output "END_OF_SCRIPT"
}