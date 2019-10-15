package com.fenghuaxz.ipay.impl;

import android.os.RemoteException;
import com.fenghuaxz.ipay.AppBridge;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import org.jdom2.Element;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

@ImplBase.Process("com.tencent.mm")
public class WechatImpl extends AppBridge.Stub implements ImplBase {

    @Override
    public String name() {
        return "微信支付";
    }

    @Override
    public void initHack(XC_LoadPackage.LoadPackageParam loadPackageParam) {



//        final ClassLoader appClassLoader = param.classLoader;
//
//        findAndHookMethod("com.tencent.wcdb.database.SQLiteDatabase", appClassLoader, "insert", String.class, String.class, ContentValues.class,
//                new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        try {
//                            ContentValues contentValues = (ContentValues) param.args[2];
//                            String tableName = (String) param.args[0];
//                            if (TextUtils.isEmpty(tableName) || !tableName.equals("message")) {
//                                return;
//                            }
//                            Integer type = contentValues.getAsInteger("type");
//                            if (null == type) {
//                                return;
//                            }
//                            if (type == 318767153) {
//                                SAXBuilder sb = new SAXBuilder();
//                                Document doc = sb.build(new StringReader(contentValues.getAsString("content")));
//
//                                JSONObject json = xmlToJson(doc.getRootElement());
//
//
//                                XposedBridge.log("收款测试: " + json);
//                                Log.e("收款", json.toString());
////                                JSONObject msg = XmlToJson.documentToJSONObject(contentValues.getAsString("content"))
////                                        .getJSONObject("appmsg");
////                                if (!msg.getString("type").equals("5")) {
////                                    //首款类型type为5
////                                    return;
////                                }
//                            }
//                        } catch (Exception e) {
//                            XposedBridge.log(e);
//                        }
//                    }
//                });
    }

    @Override
    public String makeQRCode(String desc, double amount) throws RemoteException {

        return null;
    }

    @Override
    public void pay(String qr, String desc, double amount, String password) throws RemoteException {

    }

    private static JSONObject xmlToJson(Element element) {

        JSONObject json = new JSONObject();
        try {
            List<Element> node = element.getChildren();
            Element et;
            List<Object> list;
            for (Element e : node) {
                list = new LinkedList<>();
                et = e;
                if (et.getTextTrim().equals("")) {
                    if (et.getChildren().size() == 0)
                        continue;
                    if (json.has(et.getName())) {
                        list = (List) json.get(et.getName());
                    }
                    list.add(xmlToJson(et));
                    json.put(et.getName(), list);
                } else {
                    if (json.has(et.getName())) {
                        list = (List) json.get(et.getName());
                    }
                    list.add(et.getTextTrim());
                    json.put(et.getName(), list);
                }
            }
        } catch (Exception e) {
            XposedBridge.log(e);
        }

        return json;
    }
}
