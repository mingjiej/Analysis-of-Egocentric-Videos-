
/**
 * 类IPixelConverter.java的实现描述：将三通道的RGB转换成一通道的灰度,并进行归一化处理
 * 
 *
 */
public interface IPixelConverter {
    float convert(int r, int g, int b);
}

