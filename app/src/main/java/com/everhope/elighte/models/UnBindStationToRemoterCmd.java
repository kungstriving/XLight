package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;

/**
 * Created by kongxiaoyang on 2015/4/17.
 */
public class UnBindStationToRemoterCmd extends BindStationToRemoterCmd {

    public UnBindStationToRemoterCmd() {
        setSubFunctionCode(FunctionCodes.SubFunctionCodes.UNBIND_REMOTER);
    }
}
