import sun.security.util.BitArray;

/**
 * Created by yjj on 15/12/1.
 */
public class BitArrayTest {

    public static void main(String[] args) {

        BitArray bitArray = new BitArray(64);

        for(int i = 10 ; i < 20; i++){
            bitArray.set(i, true);
        }



        System.out.println(bitArray);
    }
}
