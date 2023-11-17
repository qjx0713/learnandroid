package indi.qjx.learnandroid.common.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessUtils {

    public static String getProcessName(int pid) {
        String processName = null;
        BufferedReader cmdlineReader = null;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;

        try {
            fileInputStream = new FileInputStream("/proc/" + pid + "/cmdline");
            inputStreamReader = new InputStreamReader(fileInputStream, "iso-8859-1");
            cmdlineReader = new BufferedReader(inputStreamReader);

            StringBuilder builder = new StringBuilder();
            int num = 0;
            char ch;
            int c = cmdlineReader.read();
            while ((num = c) != -1) {
                ch = (char) num;
                builder.append(ch);
                c = cmdlineReader.read();
            }
            builder.trimToSize();
            processName = builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (cmdlineReader != null) {
                    cmdlineReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return processName;
    }

}
