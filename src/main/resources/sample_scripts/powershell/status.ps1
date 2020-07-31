. /opt/scripts/login.ps1

$aas = Get-AzAutomationAccount
$aasNames = $aas | ForEach-Object {Write-Output  "<p><span style=\'margin-left:30px;color:blue;width:400px\'>$($_.AutomationAccountName)</span> created at: $($_.CreationTime)<br></p>"}

Write-Output "{'status':'ok','message':'Script execution is successful', 'data': {'output':
        '<b>Azure subscription name:</b> $($azureContext.Subscription.Name) <br>
         <b>Azure subscription id:</b> $($azureContext.Subscription.Id) <br>
         <b>Azure tenant id:</b> $($azureContext.Tenant.Id) <br><br>
         <b>Available automation accounts:</b> $($aasNames)'}}"

Write-Output "END_OF_SCRIPT"