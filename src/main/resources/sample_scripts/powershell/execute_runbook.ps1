<#Simple script for executing existing runbook within given context#>

. /opt/scripts/login.ps1
if ($loginError -eq $true)
{
    Exit 1
}

Try
{
    $rg = "oscm-rg"
    $aa = "oscm-aaa"
    $runbook = "AzureAutomationTutorialScript"

    $runbook_exec = Start-AzAutomationRunbook -Name $runbook -ResourceGroupName $rg -AutomationAccountName $aa

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