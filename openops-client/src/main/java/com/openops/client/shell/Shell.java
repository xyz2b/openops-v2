package com.openops.client.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class Shell {
    private final String[] cmd;
    private final ProcessBuilder processBuilder;
    private final long timeout;
    private final TimeUnit unit;
    private final int outputCharNum;
    private int exitValue = -1; // -1代表运行超时，linux的exit value取值范围0-255
    private StringBuilder stdOut;
    private StringBuilder stdErr;
    private long startTime;
    private long endTime;


    public Shell(String[] cmd, long timeout, TimeUnit unit, int outputCharNum) {
        this.cmd = cmd;
        this.timeout = timeout;
        this.unit = unit;
        this.outputCharNum = outputCharNum;
        processBuilder = new ProcessBuilder(cmd);
    }

    public void start() throws IOException, InterruptedException {
        startTime = System.currentTimeMillis();
        Process process = processBuilder.start();

        // 阻塞操作，会超时
        process.waitFor(timeout, unit);

        endTime = System.currentTimeMillis();

        if (process.isAlive()) {
            process.destroy();
            return;
        }

        exitValue = process.exitValue();
        stdOut = getOutput(process.getInputStream(), outputCharNum);
        stdErr = getOutput(process.getErrorStream(), outputCharNum);
    }

    public String stdErr() {
        if (exitValue == -1) {
            return "";
        }
        return stdErr.toString();
    }

    public String stdOut() {
        if (exitValue == -1) {
            return "";
        }
        return stdOut.toString();
    }

    public int exitValue() {
        return exitValue;
    }

    public long startTime() {
        return startTime;
    }

    public long endTime() {
        return endTime;
    }

    private StringBuilder getOutput(InputStream inputStream, int outputCharNum) throws IOException {
        if (inputStream == null || outputCharNum <= 0) {
            return null;
        }

        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        StringBuilder stringBuilder = new StringBuilder();
        char[] cb = new char[1024];

        for(int i = 0; i < (outputCharNum >> 10) + 1; i++) {
            int readChars = input.read(cb);
            if(readChars > 0){
                stringBuilder.append(cb);
            } else {
                break;
            }
        }

        return stringBuilder;
    }
}