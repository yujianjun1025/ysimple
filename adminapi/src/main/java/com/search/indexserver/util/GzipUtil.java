package com.search.indexserver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 *
 * Created by yjj on 16/2/3.
 */
public class GzipUtil {

    private static final Logger logger = LoggerFactory.getLogger(GzipUtil.class);

    public static byte[] compress(byte[] input) {

        // Create the compressor with highest level of compression
        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);

        // Give the compressor the data to compress
        compressor.setInput(input);
        compressor.finish();

        // Create an expandable byte array to hold the compressed data.
        // You cannot use an array that's the same size as the orginal because
        // there is no guarantee that the compressed data will be smaller than
        // the uncompressed data.
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

        // Compress the data
        byte[] buf = new byte[1024];
        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }
        try {
            bos.close();
        } catch (IOException e) {
            logger.error("gzip 压缩出现异常", e);
        }

        return bos.toByteArray();

    }


    public static byte[] uncompress(byte[] input) {
        Inflater deCompressor = new Inflater();
        deCompressor.setInput(input);

        // Create an expandable byte array to hold the decompressed data
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

        // Decompress the data
        byte[] buf = new byte[1024];
        while (!deCompressor.finished()) {
            try {
                int count = deCompressor.inflate(buf);
                bos.write(buf, 0, count);
            } catch (DataFormatException e) {
                logger.error("gzip 解压缩出现异常", e);
            }
        }
        try {
            bos.close();
        } catch (IOException e) {
            logger.error("gzip 解压缩出现异常", e);
        }

        return bos.toByteArray();
    }
}
