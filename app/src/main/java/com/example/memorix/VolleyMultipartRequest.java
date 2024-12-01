package com.example.memorix;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {
    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final Map<String, DataPart> byteData;
    private final Response.Listener<NetworkResponse> listener;
    private final Map<String, String> headers;

    public VolleyMultipartRequest(int method, String url, Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = listener;
        this.byteData = new HashMap<>();
        this.headers = new HashMap<>();
        addDefaultHeaders();
    }

    private void addDefaultHeaders() {
        String credentials = "11193618:60-dayfreetrial";
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        headers.put("Authorization", auth);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }

    public void addDataPart(String key, DataPart dataPart) {
        byteData.put(key, dataPart);
    }

    protected Map<String, String> getParams() {
        return new HashMap<>();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        listener.onResponse(response);
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public byte[] getBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            for (Map.Entry<String, String> entry : getParams().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                dos.writeBytes("--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"\r\n");
                dos.writeBytes("\r\n");
                dos.writeBytes(value);
                dos.writeBytes("\r\n");
            }

            for (Map.Entry<String, DataPart> entry : byteData.entrySet()) {
                String key = entry.getKey();
                DataPart dataPart = entry.getValue();

                dos.writeBytes("--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + dataPart.getFileName() + "\"\r\n");
                dos.writeBytes("Content-Type: " + dataPart.getType() + "\r\n");
                dos.writeBytes("\r\n");

                dos.write(dataPart.getContent());
                dos.writeBytes("\r\n");
            }
            dos.writeBytes("--" + boundary + "--\r\n");
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}
