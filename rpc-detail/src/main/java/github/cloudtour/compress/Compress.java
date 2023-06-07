package github.cloudtour.compress;

import github.cloudtour.extension.SPI;

/**
 * 压缩接口
 * @author cloudtour
 * @version 1.0
 */
@SPI
public interface Compress {

    /**
     * 对字节数组进行压缩
     * @param bytes    要压缩的字节数组
     * @return byte[]  压缩后的字节数组
     **/
    byte[] compress(byte[] bytes);


    /**
     * 对字节数组进行解压
     * @param bytes     要解压的字节数组
     * @return byte[]   解压后的字节数组
     **/
    byte[] decompress(byte[] bytes);
}
