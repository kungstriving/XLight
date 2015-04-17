package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;

import java.nio.ByteBuffer;

/**
 * 子命令对象
 * Created by kongxiaoyang on 2015/3/21.
 */
public class StationSubCmd {
    private FunctionCodes.SubFunctionCodes subFunctionCode;

    /**
     * 从字节数组获取站点子命令定义
     * 一个站点子命令由四个字节组成
     * @param bytes
     * @return
     */
    public static StationSubCmd getStationSubCmdFromBytes(byte[] bytes) {
//        if (bytes.length != 4) {
//            throw new Exception("站点子命令消息解析错误");
//        }

        //获取子命令类型
        byte subCmdType = bytes[0];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        FunctionCodes.SubFunctionCodes subFunctionCode = FunctionCodes.SubFunctionCodes.fromSubFunctionCodeByte(subCmdType);
        switch (subFunctionCode) {

            case COLOR_TURN:
                StationColorTurnCmd stationColorTurnCmd = new StationColorTurnCmd();
                stationColorTurnCmd.setH(bytes[1]);
                stationColorTurnCmd.setS(bytes[2]);
                stationColorTurnCmd.setB(bytes[3]);
                return stationColorTurnCmd;
            case DEVICE_STATUS:
                StationDeviceStatusCmd stationDeviceStatusCmd = new StationDeviceStatusCmd();
                stationDeviceStatusCmd.setFlags(bytes[3]);
                return stationDeviceStatusCmd;
            case DEVICE_SWITCH:
                StationDeviceSwitchCmd stationDeviceSwitchCmd = new StationDeviceSwitchCmd();
                if (bytes[3] == 1) {
                    stationDeviceSwitchCmd.setSwitchStatus(true);
                } else {
                    stationDeviceSwitchCmd.setSwitchStatus(false);
                }
                return stationDeviceSwitchCmd;
            case BRIGHTNESS_TURN:
                StationBrightTurnCmd stationBrightTurnCmd = new StationBrightTurnCmd();
                stationBrightTurnCmd.setBrightValue(bytes[3]);
                return stationBrightTurnCmd;
            case BIND_REMOTER:
                BindStationToRemoterCmd bindStationToRemoterCmd = new BindStationToRemoterCmd();

                byteBuffer.get();
                bindStationToRemoterCmd.setControlNum(byteBuffer.get());
                bindStationToRemoterCmd.setStationID(byteBuffer.getShort());

                return bindStationToRemoterCmd;
            case UNBIND_REMOTER:
                UnBindStationToRemoterCmd unBindStationToRemoterCmd = new UnBindStationToRemoterCmd();
                byteBuffer.get();
                unBindStationToRemoterCmd.setControlNum(byteBuffer.get());
                unBindStationToRemoterCmd.setStationID(byteBuffer.getShort());
                return unBindStationToRemoterCmd;

            case WRONG_CODE:
                return null;
            default:
                return null;

        }
    }

    public FunctionCodes.SubFunctionCodes getSubFunctionCode() {
        return subFunctionCode;
    }

    public void setSubFunctionCode(FunctionCodes.SubFunctionCodes subFunctionCode) {
        this.subFunctionCode = subFunctionCode;
    }
}
