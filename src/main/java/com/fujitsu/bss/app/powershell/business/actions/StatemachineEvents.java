/*******************************************************************************
 *
 *  COPYRIGHT (C) 2017 FUJITSU Limited - ALL RIGHTS RESERVED.
 *
 *  Creation Date: 20.03.2017
 *
 *******************************************************************************/

package com.fujitsu.bss.app.powershell.business.actions;

public interface StatemachineEvents {

    String FINISHED = "finished";
    String SUCCESS = "success";
    String END = "end";
    String ERROR = "error";
    String FAILED = "failed";
    String EXECUTING = "executing";
    String RUN = "run";
}
