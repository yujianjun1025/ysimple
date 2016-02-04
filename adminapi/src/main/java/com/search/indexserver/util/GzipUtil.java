package com.search.indexserver.util;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 *
 * Created by yjj on 16/2/3.
 */

@Slf4j
public class GzipUtil {

    public static byte[] compress(byte[] input) {

        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_SPEED);

        compressor.setInput(input);
        compressor.finish();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
        byte[] buf = new byte[1024];
        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }
        try {
            bos.close();
        } catch (IOException e) {
            log.error("gzip 压缩出现异常", e);
        }

        return bos.toByteArray();

    }


    public static byte[] uncompress(byte[] input) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        Inflater deCompressor = new Inflater();
        deCompressor.setInput(input);

        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

        byte[] buf = new byte[1024];
        while (!deCompressor.finished()) {
            try {
                int count = deCompressor.inflate(buf);
                bos.write(buf, 0, count);
            } catch (DataFormatException e) {
                log.error("gzip 解压缩出现异常", e);
            }
        }
        try {
            bos.close();
        } catch (IOException e) {
            log.error("gzip 解压缩出现异常", e);
        }

        byte[] ret = bos.toByteArray();
        log.info("uncompress 解压前 length {} 解压后length {} 耗时 {}毫秒", input.length, ret.length, (1.0 * stopwatch.elapsed(TimeUnit.NANOSECONDS) / 1000000));
        return ret;
    }
}
