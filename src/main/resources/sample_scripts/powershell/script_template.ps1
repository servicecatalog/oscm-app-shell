. /opt/scripts/login.ps1
if ($loginError -eq $true)
{
    Exit 1
}

Try
{
    #Here all relevant steps of the scripts should be put

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