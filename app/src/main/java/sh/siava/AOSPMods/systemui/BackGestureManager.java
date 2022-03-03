package sh.siava.AOSPMods.systemui;

import android.graphics.Point;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BackGestureManager implements IXposedHookLoadPackage {
    private static final String listenPackage = "com.android.systemui";
    public static float backGestureHeightFractionLeft = 0.5f; // 50% of screen height. can be anything between 0 to 1
    public static float backGestureHeightFractionRight = 0.5f; // 50% of screen height. can be anything between 0 to 1
    public static boolean leftEnabled = true;
    public static boolean rightEnabled = true;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if(!lpparam.packageName.equals(listenPackage)) return;

        XposedHelpers.findAndHookMethod("com.android.systemui.navigationbar.gestural.EdgeBackGestureHandler", lpparam.classLoader,
                "isWithinInsets", int.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        int x = (int) param.args[0];
                        int y = (int) param.args[1];
                        Point mDisplaySize = (Point) XposedHelpers.getObjectField(param.thisObject, "mDisplaySize");
                        boolean isLeftSide = x < (mDisplaySize.x/3);

                        float mBottomGestureHeight = (float) XposedHelpers.getObjectField(param.thisObject, "mBottomGestureHeight");

                        int mEdgeHeight;
                        if(isLeftSide)
                        {
                            if(!leftEnabled)
                            {
                                param.setResult(false);
                                return;
                            }
                            mEdgeHeight = Math.round(mDisplaySize.y * backGestureHeightFractionLeft);
                        }
                        else
                        {
                            if(!rightEnabled)
                            {
                                param.setResult(false);
                                return;
                            }
                            mEdgeHeight = Math.round(mDisplaySize.y * backGestureHeightFractionRight);
                        }
  //                      XposedBridge.log("SIAPOSED: height:" + mEdgeHeight);

                        if (mEdgeHeight != 0) {
                            if (y < (mDisplaySize.y - mBottomGestureHeight - mEdgeHeight)) {
//                                XposedBridge.log("SIAPOSED back i didn't approve" + mEdgeHeight);
                                param.setResult(false);
                                return;
                            }
                        }
                    }
                });
    }


}