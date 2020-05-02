package com.sunmi.innerprinter;

public class ServiceActionConsts {
    // 打印机准备中
    public final static String INIT_ACTION = "woyou.aidlservice.jiuv5.INIT_ACTION";
    // 打印机更更新中
    public final static String FIRMWARE_UPDATING_ACITON = "woyou.aidlservice.jiuv5.FIRMWARE_UPDATING_ACITON";
    // 可以打印
    public final static String NORMAL_ACTION = "woyou.aidlservice.jiuv5.NORMAL_ACTION";
    // 打印错误
    public final static String ERROR_ACTION = "woyou.aidlservice.jiuv5.ERROR_ACTION";
    // 缺纸异常
    public final static String OUT_OF_PAPER_ACTION = "woyou.aidlservice.jiuv5.OUT_OF_PAPER_ACTION";
    // 打印头过热异常
    public final static String OVER_HEATING_ACITON = "woyou.aidlservice.jiuv5.OVER_HEATING_ACITON";
    // 打印头温度恢复正常
    public final static String NORMAL_HEATING_ACITON = "woyou.aidlservice.jiuv5.NORMAL_HEATING_ACITON";
    // 开盖子
    public final static String COVER_OPEN_ACTION = "woyou.aidlservice.jiuv5.COVER_OPEN_ACTION";
    // 关盖子异常
    public final static String COVER_ERROR_ACTION = "woyou.aidlservice.jiuv5.COVER_ERROR_ACTION";
    // 切刀异常1－卡切刀
    public final static String KNIFE_ERROR_1_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_1";
    // 切刀异常2－切刀修复
    public final static String KNIFE_ERROR_2_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_2";
    // 未发现打印机
    public final static String PRINTER_NON_EXISTENT_ACITON = "woyou.aidlservice.jiuv5.PRINTER_NON_EXISTENT_ACITON";
    // 未检测到⿊黑标
    public final static String BLACKLABEL_NON_EXISTENT_ACITON = "woyou.aidlservice.jiuv5.BLACKLABEL_NON_EXISTENT_ACITON";
}
