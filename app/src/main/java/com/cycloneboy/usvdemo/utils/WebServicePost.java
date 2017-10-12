/**
 * 文件名    :WebService.java
 * 项目名称:AndroidHTTPDemo
 * 描述信息:
 * 版本信息: V1.0
 * 创建日期:2017年4月19日
 * 作者        :CycloneBoy
 */
package com.cycloneboy.usvdemo.utils;

import android.util.Log;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CycloneBoy
 *
 */

public class WebServicePost {

    private static String IP = "120.25.197.25:8080";

    // 通过 POST 方式获取HTTP服务器数据
    public static String executeHttpPost(String username, String password) {

        try {
            String path = "http://" + IP + "/HelloWeb/RegLet";

            // 发送指令和信息
            Map<String, String> params = new HashMap<String, String>();
            params.put("username", username);
            params.put("password", password);
            Log.i("info","注册URL:"+path+" ,username="+username+" ,password="+password, null);
            return sendPOSTRequest(path, params, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // 通过Get 方式获取HTTP服务器数据
    public static String executeHttpGet(String imgname, String name,
                                        String degree, String position, String findtime) {
    	 HttpURLConnection conn = null;
         InputStream is = null;

         try {
             // 用户名 密码
             // URL 地址
             String path = "http://" + IP + "/HelloWeb/InsertLet";
             path = path + "?imgname=" + imgname + "&name=" + name+
            		 "&degree=" + degree + "&position=" + position + "&findtime="+
            		 findtime;
             Log.i("info", path, null);
             
             conn = (HttpURLConnection) new URL(path).openConnection();
             conn.setConnectTimeout(3000); // 设置超时时间
             conn.setReadTimeout(3000);
             conn.setDoInput(true);
             conn.setRequestMethod("GET"); // 设置获取信息方式
             conn.setRequestProperty("Charset", "UTF-8"); // 设置接收数据编码格式

             is = conn.getInputStream();
             Log.i("info","成功上传数据到从服务器:"+path, null);
             return parseInfo(is);
             
//             if (conn.getResponseCode() == 200) {
//                 is = conn.getInputStream();
//                 Log.i("info","成功上传数据到从服务器:"+path, null);
//                // return parseInfo(is);
//             }

         }catch (Exception e) {
             e.printStackTrace();
         } finally {
             // 意外退出时进行连接关闭保护
             if (conn != null) {
                 conn.disconnect();
             }
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }

         }
         return null;
    	
    }
    
    // 通过Get 方式获取HTTP服务器数据
    public static String executeHttpFindByPName(String name) {
    	 HttpURLConnection conn = null;
         InputStream is = null;

         try {
             // 用户名 密码
             // URL 地址
             String path = "http://" + IP + "/HelloWeb/FindByPName";
             path = path + "?name=" + name;
             
             Log.i("info", path, null);
             
             conn = (HttpURLConnection) new URL(path).openConnection();
             conn.setConnectTimeout(3000); // 设置超时时间
             conn.setReadTimeout(3000);
             conn.setDoInput(true);
             conn.setRequestMethod("GET"); // 设置获取信息方式
             conn.setRequestProperty("Charset", "UTF-8"); // 设置接收数据编码格式

             is = conn.getInputStream();
             Log.i("info","成功从服务器获取数据:"+path, null);
             return parseInfo(is);
             
//             if (conn.getResponseCode() == 200) {
//                 is = conn.getInputStream();
//                 Log.i("info","成功上传数据到从服务器:"+path, null);
//                // return parseInfo(is);
//             }

         }catch (Exception e) {
             e.printStackTrace();
         } finally {
             // 意外退出时进行连接关闭保护
             if (conn != null) {
                 conn.disconnect();
             }
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }

         }
         return null;
    	
    }
    
 // 通过Get 方式获取HTTP服务器数据
    public static String executeHttpFindByImgname(String imgname) {
    	 HttpURLConnection conn = null;
         InputStream is = null;

         try {
             // 用户名 密码
             // URL 地址
             String path = "http://" + IP + "/HelloWeb/FindByImgname";
             path = path + "?imgname=" + imgname;
             
             Log.i("info", path, null);
             
             conn = (HttpURLConnection) new URL(path).openConnection();
             conn.setConnectTimeout(3000); // 设置超时时间
             conn.setReadTimeout(3000);
             conn.setDoInput(true);
             conn.setRequestMethod("GET"); // 设置获取信息方式
             conn.setRequestProperty("Charset", "UTF-8"); // 设置接收数据编码格式

             is = conn.getInputStream();
             Log.i("info","成功从服务器获取数据:"+path, null);
             return parseInfo(is);
             
//             if (conn.getResponseCode() == 200) {
//                 is = conn.getInputStream();
//                 Log.i("info","成功上传数据到从服务器:"+path, null);
//                // return parseInfo(is);
//             }

         }catch (Exception e) {
             e.printStackTrace();
         } finally {
             // 意外退出时进行连接关闭保护
             if (conn != null) {
                 conn.disconnect();
             }
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }

         }
         return null;
    	
    }
    
    // 处理发送数据请求
    @SuppressWarnings("deprecation")
	private static String sendPOSTRequest(String path, Map<String, String> params, String encoding) throws Exception {

//        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
//        if (params != null && !params.isEmpty()) {
//            for (Map.Entry<String, String> entry : params.entrySet()) {
//                pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
//            }
//        }
//
//        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, encoding);
//
//        HttpPost post = new HttpPost(path);
//        post.setEntity(entity);
//        DefaultHttpClient client = new DefaultHttpClient();
//        // 请求超时
//        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
//        // 读取超时
//        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
//        HttpResponse response = client.execute(post);
//
//        String reString =getInfo(response);
//        Log.i("info",reString, null);
//        // 判断是否成功收取信息
//        if (response.getStatusLine().getStatusCode() == 200) {
//        	Log.i("info","成功的post了数据", null);
//            return getInfo(response);
//        }
//
//        // 未成功收取信息，返回空指针
        return null;
    }

    // 将输入流转化为 String 型 
    private static String parseInfo(InputStream inStream) throws Exception {
        byte[] data = read(inStream);
        // 转化为字符串
        return new String(data, "UTF-8");
    }
    
    // 将输入流转化为byte型 
    public static byte[] read(InputStream inStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        inStream.close();
        return outputStream.toByteArray();
    }
    
//    // 收取数据
//    private static String getInfo(HttpResponse response) throws Exception {
//
//        HttpEntity entity = response.getEntity();
//        InputStream is = entity.getContent();
//        // 将输入流转化为byte型
//        byte[] data = read(is);
//        // 转化为字符串
//        return new String(data, "UTF-8");
//    }
}