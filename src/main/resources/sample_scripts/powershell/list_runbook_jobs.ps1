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

    $jobs = Get-AzAutomationJob -ResourceGroupName $rg -AutomationAccountName $aa
    $jobsDetails = $jobs | ForEach-Object { Write-Output  "<p><span style=\'margin-left:30px;color:blue;width:400px\'>$( $_.Status )</span> started at: $( $_.StartTime )<br></p>" }

    Write-Output "{'status':'ok','message':'Script execution is successful', 'data': {'output':'Recent jobs of <b>$( $runbook )</b>  <br>$( $jobsDetails )'}}"
    Write-Output "END_OF_SCRIPT"
}
Catch
{
    $errorCommand = $PSItem.InvocationInfo.MyCommand
    $errorMsg = $_.Exception.Message -replace "'", ""
    Write-Output "{'status':'error','message':'$( $errorCommand ): $( $errorMsg )'}"
    Write-Output "END_OF_SCRIPT"
}