package com.search.engine.cache;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.search.engine.pojo.Node;
import com.search.engine.util.SerializeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Created by yjj on 15/12/12.
 */
public class ForwardCache {


    private static final Logger logger = LoggerFactory.getLogger(ForwardCache.class);


    private static int ARR_LENGTH = 100000;
    FileOutputStream fos = null;
    int offset = 0;
    FileChannel fc = null;
    //高32位为开始地址， 第32位为结束位置
    private List<List<Position>> address = Lists.newArrayList();

    public static ForwardCache getInstance() {
        return ForwardCacheHolder.instance;
    }

    public static void main(String[] args) {

        String path = "/Users/yjj/m/search_engine/target/classes/forward.dat";
        ForwardCache forwardCache = ForwardCache.getInstance();

        try {

            /*forwardCache.mem2diskBefore(path);
            List<Node> nodeList = Lists.newArrayList(new Node(0, Lists.newArrayList(1, 2, 3), 2.0));
            forwardCache.addDocument(0, nodeList);*/
            forwardCache.mem2diskEnd(path);

            for (int i = 1; i < 99; i++) {

                List<Node> nodeList = forwardCache.getNodeByDocId(i);
                System.out.println("nodeList:" + Joiner.on(" ").join(nodeList));
            }

            /*int k = 0;
            while (k++ < 10000) {
                for (List<Position> positionList : forwardCache.getAddress()) {
                    for (Position position : positionList) {
                        long begin = System.nanoTime();
                        List<Node> nodes = forwardCache.getNodeByPosition(position);
                        long end = System.nanoTime();
                        System.out.println("反序列耗时" + (end - begin) * 1.0 / 1000000 + "毫秒");
                    }
                }
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void mem2diskBefore(String path) throws FileNotFoundException {
        fos = new FileOutputStream(path);
    }

    public void addDocument(int docId, List<Node> nodeList) throws IOException {
        byte[] bytes = SerializeUtil.serialize(nodeList);
        int size = bytes.length;
        fos.write(bytes);

        int level = docId / ARR_LENGTH;
        if (level == address.size()) {
            address.add(Lists.<Position>newArrayList());
        } else if (level > address.size()) {
            logger.error("严重错误 level:{}, address size{}", level, address.size());
        }
        List<Position> positions = address.get(level);
        int mod = docId - ARR_LENGTH * level;

        if (mod == positions.size()) {
            positions.add(mod, new Position(offset, size));
            offset += size;
        } else if (mod > positions.size()) {
            logger.error("严重错误 mod:{}, position size{}", mod, positions.size());
        }

    }

    public void mem2diskEnd(String path) throws IOException {

        if (fos != null) {
            fos.close();
        }
        //磁盘映射到内存
        fc = new FileInputStream(path).getChannel();
    }

    public List<Node> getNodeByPosition(Position position) throws IOException, ClassNotFoundException {

        //todo
        if (position.getOffset() < 0 || position.getSize() < 0) {
            return Lists.newArrayList();
        }

        MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, position.offset, position.size);
        byte[] bytes = new byte[position.size + 1];
        byteBuffer.get(bytes, 0, position.size);
        byteBuffer.clear();
        return (List<Node>) SerializeUtil.deserialize(bytes);
    }

    public List<Node> getNodeByDocId(int docId) throws IOException, ClassNotFoundException {

        int level = docId / ARR_LENGTH;
        int mod = docId - ARR_LENGTH * level;
        Position position = address.get(level).get(mod);
        return getNodeByPosition(position);
    }

    public List<List<Position>> getAddress() {
        return address;
    }

    public void setAddress(List<List<Position>> address) {
        this.address = address;
    }

    public static final class ForwardCacheHolder {
        private static final ForwardCache instance = new ForwardCache();
    }

    class Position {
        private int offset;
        private int size;

        public Position(int offset, int size) {
            this.offset = offset;
            this.size = size;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        @Override
        public String toString() {
            return "Position{" +
                    "offset=" + offset +
                    ", size=" + size +
                    '}';
        }
    }

}
