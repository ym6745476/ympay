package com.ymbok.pay.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * 使用了google zxing作为二维码生成工具
 * @author ymbok.com(ym6745476)
 * @date 2020-06-30 14:38
 */
public class ZxingUtil {
    private static Log log = LogFactory.getLog(ZxingUtil.class);

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    private static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }

    private static void writeToFile(BitMatrix matrix, String format, File file) throws IOException {
        BufferedImage image = toBufferedImage(matrix);
        if (!ImageIO.write(image, format, file)) {
            throw new IOException("Could not write an image of format " + format + " to " + file);
        }
    }

    /**
     * 将内容contents生成长宽均为width的图片，图片路径由imgPath指定
     */
    public static File getQrCodeImage(String contents, int width, String imgPath) {
        return getQrCodeImage(contents, width, width, imgPath);
    }

    /**
     * 将内容contents生成长为width，宽为width的图片，图片路径由imgPath指定
     */
    public static File getQrCodeImage(String contents, int width, int height, String imgPath) {
        try {
            Map<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF8");

            BitMatrix bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, width, height, hints);

            File imageFile = new File(imgPath);
            writeToFile(bitMatrix, "png", imageFile);

            return imageFile;

        } catch (Exception e) {
            log.error("create QR code error!", e);
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        //二维码中保存的信息
        String outTradeNo = String.valueOf(System.currentTimeMillis());
        String content = "http://205v2077a1.imwork.net:58663/trade/wap/pay?outTradeNo=" + outTradeNo;
        //生成的二维码保存的路径
        String path = "C:/Users/change/Desktop/";
        MultiFormatWriter multiFormatWrite = new MultiFormatWriter();
        Map<EncodeHintType, String> hints = new HashMap<>(1);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 按照指定的宽度，高度和附加参数对字符串进行编码
        //生成二维码
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        BitMatrix bitMatrix = multiFormatWrite.encode(content, BarcodeFormat.QR_CODE, 400, 400, hints);
        File file1 = new File(path, outTradeNo + ".jpg");
        // 写入文件
        writeToFile(bitMatrix, "jpg", file1);
        System.out.println("二维码图片生成成功！");
    }
}
