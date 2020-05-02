package com.sunmi.innerprinter;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.InnerResultCallbcak;
import com.sunmi.peripheral.printer.SunmiPrinterService;
import com.sunmi.peripheral.printer.WoyouConsts;

import java.util.HashMap;
import java.util.Map;

public class SunmiInnerPrinterModule extends ReactContextBaseJavaModule {
    public static ReactApplicationContext reactApplicationContext = null;
    private SunmiPrinterService sunmiPrinterService;
    private BitmapUtils bitMapUtils;

    private static int NoSunmiPrinter = 0x00000000;
    private static int CheckSunmiPrinter = 0x00000001;
    private static int FoundSunmiPrinter = 0x00000002;
    private static int LostSunmiPrinter = 0x00000003;

    private PrinterReceiver receiver = new PrinterReceiver();


    private static final String TAG = "SunmiInnerPrinterModule";
    /**
     * sunmiPrinter means checking the printer connection status
     */
    public int sunmiPrinter = CheckSunmiPrinter;

    public SunmiInnerPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactApplicationContext = reactContext;
        initSunmiPrinterService(reactContext);
        bitMapUtils = new BitmapUtils(reactContext);
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ServiceActionConsts.INIT_ACTION);
        mFilter.addAction(ServiceActionConsts.FIRMWARE_UPDATING_ACITON);
        mFilter.addAction(ServiceActionConsts.NORMAL_ACTION);
        mFilter.addAction(ServiceActionConsts.ERROR_ACTION);
        mFilter.addAction(ServiceActionConsts.OUT_OF_PAPER_ACTION);
        mFilter.addAction(ServiceActionConsts.OVER_HEATING_ACITON);
        mFilter.addAction(ServiceActionConsts.NORMAL_HEATING_ACITON);
        mFilter.addAction(ServiceActionConsts.COVER_OPEN_ACTION);
        mFilter.addAction(ServiceActionConsts.COVER_ERROR_ACTION);
        mFilter.addAction(ServiceActionConsts.KNIFE_ERROR_1_ACTION);
        mFilter.addAction(ServiceActionConsts.KNIFE_ERROR_2_ACTION);
        mFilter.addAction(ServiceActionConsts.PRINTER_NON_EXISTENT_ACITON);
        mFilter.addAction(ServiceActionConsts.BLACKLABEL_NON_EXISTENT_ACITON);
        getReactApplicationContext().registerReceiver(receiver, mFilter);
        Log.d("PrinterReceiver", "------------ init ");
    }

    private static byte[] rnArrayToBytes(ReadableArray rArray) {
        byte[] bytes = new byte[rArray.size()];
        for (int i = 0; i < rArray.size(); i++) {
            bytes[i] = (byte)(rArray.getInt(i) & 0xff);
        }
        return bytes;
    }

    private InnerPrinterCallback innerPrinterCallback = new InnerPrinterCallback() {
        @Override
        protected void onConnected(SunmiPrinterService service) {
            Log.i(TAG, "Sunmi inner printer service connected.");
            sunmiPrinterService = service;
            checkSunmiPrinterService(service);
        }

        @Override
        protected void onDisconnected() {
            Log.i(TAG, "Sunmi inner printer service disconnected.");
            sunmiPrinterService = null;
            sunmiPrinter = LostSunmiPrinter;
        }
    };

    /**
     * Check the printer connection,
     * like some devices do not have a printer but need to be connected to the cash drawer through a print service
     */
    private void checkSunmiPrinterService(SunmiPrinterService service) {
        boolean ret = false;
        try {
            ret = InnerPrinterManager.getInstance().hasPrinter(service);
        } catch (InnerPrinterException e) {
            e.printStackTrace();
        }
        sunmiPrinter = ret ? FoundSunmiPrinter : NoSunmiPrinter;
    }

    /**
     * init sunmi print service
     */
    public void initSunmiPrinterService(Context context) {
        try {
            boolean ret = InnerPrinterManager.getInstance().bindService(context, innerPrinterCallback);
            if (!ret) {
                sunmiPrinter = NoSunmiPrinter;
            }
        } catch (InnerPrinterException e) {
            e.printStackTrace();
        }
    }
    @Override
    public String getName() {
        return "SunmiInnerPrinter";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        final Map<String, Object> constantsChildren = new HashMap<>();

        constantsChildren.put("INIT_ACTION", ServiceActionConsts.INIT_ACTION);
        constantsChildren.put("FIRMWARE_UPDATING_ACITON", ServiceActionConsts.FIRMWARE_UPDATING_ACITON);
        constantsChildren.put("NORMAL_ACTION", ServiceActionConsts.NORMAL_ACTION);
        constantsChildren.put("ERROR_ACTION", ServiceActionConsts.ERROR_ACTION);
        constantsChildren.put("OUT_OF_PAPER_ACTION", ServiceActionConsts.OUT_OF_PAPER_ACTION);
        constantsChildren.put("OVER_HEATING_ACITON", ServiceActionConsts.OVER_HEATING_ACITON);
        constantsChildren.put("NORMAL_HEATING_ACITON", ServiceActionConsts.NORMAL_HEATING_ACITON);
        constantsChildren.put("COVER_OPEN_ACTION", ServiceActionConsts.COVER_OPEN_ACTION);
        constantsChildren.put("COVER_ERROR_ACTION", ServiceActionConsts.COVER_ERROR_ACTION);
        constantsChildren.put("KNIFE_ERROR_1_ACTION", ServiceActionConsts.KNIFE_ERROR_1_ACTION);
        constantsChildren.put("KNIFE_ERROR_2_ACTION", ServiceActionConsts.KNIFE_ERROR_2_ACTION);
        constantsChildren.put("PRINTER_NON_EXISTENT_ACITON", ServiceActionConsts.PRINTER_NON_EXISTENT_ACITON);
        constantsChildren.put("BLACKLABEL_NON_EXISTENT_ACITON", ServiceActionConsts.BLACKLABEL_NON_EXISTENT_ACITON);

        constants.put("Constants", constantsChildren);
        constants.put("hasPrinter", hasPrinter());

        try {
            constants.put("printerVersion", getPrinterVersion());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
        try {
            constants.put("printerSerialNo", getPrinterSerialNo());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
        try {
            constants.put("printerModal", getPrinterModal());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
        constants.put("MODEL", Build.MODEL);
        try {
            constants.put("printerPaper", getPrinterPaper());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
        try {
            constants.put("printerState", updatePrinterState());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
        try {
            constants.put("serviceVersion", getServiceVersion());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
        }

        // 样式
        constants.put("ENABLE", WoyouConsts.ENABLE);
        constants.put("DISABLE", WoyouConsts.DISABLE);
        constants.put("ENABLE_DOUBLE_WIDTH", WoyouConsts.ENABLE_DOUBLE_WIDTH);
        constants.put("ENABLE_DOUBLE_HEIGHT", WoyouConsts.ENABLE_DOUBLE_HEIGHT);
        constants.put("ENABLE_BOLD", WoyouConsts.ENABLE_BOLD);
        constants.put("ENABLE_UNDERLINE", WoyouConsts.ENABLE_UNDERLINE);
        constants.put("ENABLE_ANTI_WHITE", WoyouConsts.ENABLE_ANTI_WHITE);
        constants.put("ENABLE_STRIKETHROUGH", WoyouConsts.ENABLE_STRIKETHROUGH);
        constants.put("ENABLE_ILALIC", WoyouConsts.ENABLE_ILALIC);
        constants.put("ENABLE_INVERT", WoyouConsts.ENABLE_INVERT);
        constants.put("SET_TEXT_RIGHT_SPACING", WoyouConsts.SET_TEXT_RIGHT_SPACING);
        constants.put("SET_RELATIVE_POSITION", WoyouConsts.SET_RELATIVE_POSITION);
        constants.put("SET_ABSOLUATE_POSITION", WoyouConsts.SET_ABSOLUATE_POSITION);
        constants.put("SET_LINE_SPACING", WoyouConsts.SET_LINE_SPACING);
        constants.put("SET_LEFT_SPACING", WoyouConsts.SET_LEFT_SPACING);
        constants.put("SET_STRIKETHROUGH_STYLE", WoyouConsts.SET_STRIKETHROUGH_STYLE);

        return constants;
    }


    /**
     * 初始化打印机，重置打印机的逻辑程序，但不清空缓存区数据，因此
     * 未完成的打印作业将在重置后继续
     */
    @ReactMethod
    public void printerInit(final Promise p) {
        final SunmiPrinterService printerService = sunmiPrinterService;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.printerInit(new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 打印机自检，打印机会打印自检页
     */
    @ReactMethod
    public void printerSelfChecking(final Promise p) {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.printerSelfChecking(new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 获取打印机板序列号
     */
    @ReactMethod
    public void getPrinterSerialNo(final Promise p) {
        try {
            p.resolve(getPrinterSerialNo());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("0", e.getMessage());
        }
    }

    private String getPrinterSerialNo() throws Exception {
        return sunmiPrinterService.getPrinterSerialNo();
    }

    /**
     * 获取打印机固件版本号
     */
    @ReactMethod
    public void getPrinterVersion(final Promise p) {
        try {
            p.resolve(getPrinterVersion());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    private String getPrinterVersion() throws Exception {
        return sunmiPrinterService.getPrinterVersion().replace("\n", "");
    }

    /**
     * 获取打印机型号
     */
    @ReactMethod
    public void getPrinterModal(final Promise p) {
        try {
            p.resolve(getPrinterModal());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("0", e.getMessage());
        }
    }

    private String getPrinterModal() throws Exception {
        return sunmiPrinterService.getPrinterModal().replace("\n", "");
    }

    /**
     * 获取打印机的最新状态
     */
    @ReactMethod
    public void updatePrinterState(final Promise p) {
        try {
            p.resolve(updatePrinterState());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("0", e.getMessage());
        }
    }

    private int updatePrinterState() throws Exception {
        return sunmiPrinterService.updatePrinterState();
    }

    /**
     * 获取打印服务版本号
     */
    @ReactMethod
    public void getServiceVersion(final Promise p) {
        try {
            p.resolve(getServiceVersion());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("0", e.getMessage());
        }
    }

    private String getServiceVersion() throws Exception {
        return sunmiPrinterService.getServiceVersion();
    }

    @ReactMethod
    public void hasPrinter(final Promise p) {
        try {
            p.resolve(hasPrinter());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    /**
     * 是否存在打印机服务
     * return {boolean}
     */
    private boolean hasPrinter() {
        return sunmiPrinterService != null;
    }

    /**
     * 获取打印头打印长度
     */
    @ReactMethod
    public void getPrintedLength(final Promise p) {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.getPrintedLength(new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }


    /**
     * 获取打印服务版本号
     */
    @ReactMethod
    public void getPrinterPaper(final Promise p) {
        try {
            p.resolve(getPrinterPaper());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("0", e.getMessage());
        }
    }

    private int getPrinterPaper() throws Exception {
        return sunmiPrinterService.getPrinterPaper();
    }

    /**
     * 打印机走纸(强制换行，结束之前的打印内容后走纸n行)
     *
     * @param n:       走纸行数
     * @return
     */
    @ReactMethod
    public void lineWrap(int n, final Promise p) {
        final int count = n;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.lineWrap(count, new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 使用原始指令打印
     *
     * @param dataArray     指令
     */
    @ReactMethod
    public void sendRAWData(ReadableArray dataArray, final Promise p) {
        final byte[] d = rnArrayToBytes(dataArray);
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.sendRAWData(d, new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 设置对齐模式，对之后打印有影响，除非初始化
     *
     * @param alignment: 对齐方式 0--居左 , 1--居中, 2--居右
     */
    @ReactMethod
    public void setAlignment(int alignment, final Promise p) {
        final int align = alignment;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.setAlignment(align, new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 设置打印字体, 对之后打印有影响，除非初始化
     * (目前只支持一种字体"gh"，gh是一种等宽中文字体，之后会提供更多字体选择)
     *
     * @param typeface: 字体名称
     */
    @ReactMethod
    public void setFontName(String typeface, final Promise p) {
        final String tf = typeface;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.setFontName(tf, new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 设置字体大小, 对之后打印有影响，除非初始化
     * 注意：字体大小是超出标准国际指令的打印方式，
     * 调整字体大小会影响字符宽度，每行字符数量也会随之改变，
     * 因此按等宽字体形成的排版可能会错乱
     *
     * @param fontsize: 字体大小
     */
    @ReactMethod
    public void setFontSize(float fontsize, final Promise p) {
        final float fs = fontsize;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.setFontSize(fs, new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }


    /**
     * 打印指定字体的文本，字体设置只对本次有效
     *
     * @param text:     要打印文字
     * @param typeface: 字体名称（目前只支持"gh"字体）
     * @param fontsize: 字体大小
     */
    @ReactMethod
    public void printTextWithFont(String text, String typeface, float fontsize, final Promise p) {
        final String txt = text;
        final String tf = typeface;
        final float fs = fontsize;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.printTextWithFont(txt, tf, fs, new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 打印表格的一行，可以指定列宽、对齐方式
     *
     * @param colsTextArr  各列文本字符串数组
     * @param colsWidthArr 各列宽度数组(以英文字符计算, 每个中文字符占两个英文字符, 每个宽度大于0)
     * @param colsAlign    各列对齐方式(0居左, 1居中, 2居右)
     *                     备注: 三个参数的数组长度应该一致, 如果colsText[i]的宽度大于colsWidth[i], 则文本换行
     */
    @ReactMethod
    public void printColumnsText(ReadableArray colsTextArr, ReadableArray colsWidthArr, ReadableArray colsAlign, final Promise p) {
        final String[] clst = new String[colsTextArr.size()];
        for (int i = 0; i < colsTextArr.size(); i++) {
            clst[i] = colsTextArr.getString(i);
        }
        final int[] clsw = new int[colsWidthArr.size()];
        for (int i = 0; i < colsWidthArr.size(); i++) {
            clsw[i] = colsWidthArr.getInt(i);
        }
        final int[] clsa = new int[colsAlign.size()];
        for (int i = 0; i < colsAlign.size(); i++) {
            clsa[i] = colsAlign.getInt(i);
        }
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.printColumnsText(clst, clsw, clsa, new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }


    /**
     * 打印图片
     *
     * @param bitmap: 图片bitmap对象(最大宽度384像素，超过无法打印并且回调promise异常函数)
     */
    @ReactMethod
    public void printBitmap(String data, int width, int height, final Promise p) {
        try {
            byte[] decoded = Base64.decode(data, Base64.DEFAULT);
            final Bitmap bitMap = bitMapUtils.decodeBitmap(decoded, width, height);
            ThreadPoolManager.getInstance().executeTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        sunmiPrinterService.printBitmap(bitMap, new InnerResultCallbcak() {
                            @Override
                            public void onPrintResult(int par1, String par2) {
                                Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                            }

                            @Override
                            public void onRunResult(boolean isSuccess) {
                                if (isSuccess) {
                                    p.resolve(null);
                                } else {
                                    p.reject("0", isSuccess + "");
                                }
                            }

                            @Override
                            public void onReturnString(String result) {
                                p.resolve(result);
                            }

                            @Override
                            public void onRaiseException(int code, String msg) {
                                p.reject("" + code, msg);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i(TAG, "ERROR: " + e.getMessage());
                        p.reject("" + 0, e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
    }

    /**
     * 打印一维条码
     *
     * @param data:         条码数据
     * @param symbology:    条码类型
     *                      0 -- UPC-A，
     *                      1 -- UPC-E，
     *                      2 -- JAN13(EAN13)，
     *                      3 -- JAN8(EAN8)，
     *                      4 -- CODE39，
     *                      5 -- ITF，
     *                      6 -- CODABAR，
     *                      7 -- CODE93，
     *                      8 -- CODE128
     * @param height:       条码高度, 取值1到255, 默认162
     * @param width:        条码宽度, 取值2至6, 默认2
     * @param textposition: 文字位置 0--不打印文字, 1--文字在条码上方, 2--文字在条码下方, 3--条码上下方均打印
     */
    @ReactMethod
    public void printBarCode(String data, int symbology, int height, int width, int textposition, final Promise p) {
        Log.i(TAG, "come: ss:" + sunmiPrinterService);
        final String d = data;
        final int s = symbology;
        final int h = height;
        final int w = width;
        final int tp = textposition;

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.printBarCode(d, s, h, w, tp, new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 打印二维条码
     *
     * @param data:       二维码数据
     * @param modulesize: 二维码块大小(单位:点, 取值 1 至 16 )
     * @param errorlevel: 二维码纠错等级(0 至 3)，
     *                    0 -- 纠错级别L ( 7%)，
     *                    1 -- 纠错级别M (15%)，
     *                    2 -- 纠错级别Q (25%)，
     *                    3 -- 纠错级别H (30%)
     */
    @ReactMethod
    public void printQRCode(String data, int modulesize, int errorlevel, final Promise p) {
        Log.i(TAG, "come: ss:" + sunmiPrinterService);
        final String d = data;
        final int size = modulesize;
        final int level = errorlevel;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.printQRCode(d, size, level, new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 打印文字，文字宽度满一行自动换行排版，不满一整行不打印除非强制换行
     * 文字按矢量文字宽度原样输出，即每个字符不等宽
     *
     * @param text: 要打印的文字字符串
     */
    @ReactMethod
    public void printOriginalText(String text, final Promise p) {
        Log.i(TAG, "come: " + text + " ss:" + sunmiPrinterService);
        final String txt = text;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.printOriginalText(txt, new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * 打印缓冲区内容
     */
    @ReactMethod
    public void commitPrinterBuffer() {
        Log.i(TAG, "come: commit buffter ss:" + sunmiPrinterService);
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.commitPrinterBuffer();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }

    /**
     * 进入缓冲模式，所有打印调用将缓存，调用commitPrinterBuffe()后打印
     *
     * @param clean: 是否清除缓冲区内容
     */
    @ReactMethod
    public void enterPrinterBuffer(boolean clean) {
        Log.i(TAG, "come: " + clean + " ss:" + sunmiPrinterService);
        final boolean c = clean;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.enterPrinterBuffer(c);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }

    /**
     * 退出缓冲模式
     *
     * @param commit: 是否打印出缓冲区内容
     */
    @ReactMethod
    public void exitPrinterBuffer(boolean commit) {
        Log.i(TAG, "come: " + commit + " ss:" + sunmiPrinterService);
        final boolean com = commit;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.exitPrinterBuffer(com);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }


    @ReactMethod
    public void printText(String message, final Promise p) {
        Log.i(TAG, "come: " + message + " ss:" + sunmiPrinterService);
        final String msgs = message;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.printText(msgs, new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("0", e.getMessage());
                }
            }
        });
    }

    @ReactMethod
    public void clearBuffer() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.clearBuffer();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }

    @ReactMethod
    public void exitPrinterBufferWithCallback(final boolean commit, final Promise p) {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.exitPrinterBufferWithCallback(commit, new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int code, String msg) {
                            Log.d(TAG, "ON PRINT RES: " + code + ", " + msg);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            Log.i(TAG, "ERROR: " + code + ", " + msg);
                            p.reject("" +code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("0", e.getMessage());
                }
            }
        });
    }

    @ReactMethod
    public void autoOutPaper(final Promise p) {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.autoOutPaper(new InnerResultCallbcak() {
                        @Override
                        public void onPrintResult(int code, String msg) {
                            Log.d(TAG, "ON PRINT RES: " + code + ", " + msg);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            Log.i(TAG, "ERROR: " + code + ", " + msg);
                            p.reject("" +code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("0", e.getMessage());
                }
            }
        });
    }
    @ReactMethod
    public void setPrinterStyle(final int key, final int value, final Promise p) {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    sunmiPrinterService.setPrinterStyle(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("0", e.getMessage());
                }
            }
        });
    }
}
