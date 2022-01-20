package smart.lib;


import smart.lib.session.Session;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 验证码
 */
public class Captcha {
    // 验证码长度
    public final static long SIZE = 4;
    private static final char[] mapTable = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '0', '1',
            '2', '3', '4', '5', '6', '7',
            '8', '9'};

    /**
     * 删除会话中的验证码
     *
     * @param session 要删除的会话
     */
    public static void clear(Session session) {
        if (session == null) {
            return;
        }
        session.delete(Helper.camelCase(Captcha.class));
    }

    /**
     * 获取验证码(size: 90px * 32px)
     *
     * @return 验证码
     */
    public static CaptchaResult getImageCode() {
        int width = 90, height = 32;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 获取图形上下文
        Graphics g = image.getGraphics();
        //生成随机类
        Random random = new Random();
        // 设定背景色
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        //设定字体
        g.setFont(new Font("Times New Roman", Font.PLAIN, 28));
        // 随机产生168条干扰线，使图象中的认证码不易被其它程序探测到
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 168; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        //取随机产生的码
        StringBuilder phrase = new StringBuilder();
        //4代表4位验证码,如果要生成更多位的认证码,则加大数值
        for (int i = 0; i < Captcha.SIZE; ++i) {
            phrase.append(mapTable[(int) (mapTable.length * Math.random())]);
            // 将认证码显示到图象中
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            // 直接生成
            String str = phrase.substring(i, i + 1);
            // 设置随便码在背景图图片上的位置
            g.drawString(str, 15 * i + 15, 26);
        }
        // 释放图形上下文
        g.dispose();
        return new CaptchaResult(image, phrase.toString());
    }

    /**
     * 给定范围获得随机颜色
     * @param fc color
     * @param bc color
     * @return Color
     */
    static Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    public static class CaptchaResult {

        private final BufferedImage image;
        private final String phrase;

        public CaptchaResult(BufferedImage image, String phrase) {
            this.image = image;
            this.phrase = phrase;
        }

        /**
         * 获取验证码图片实例
         *
         * @return image
         */
        public BufferedImage getImage() {
            return image;
        }

        /**
         * 获取验证码
         *
         * @return 验证码
         */
        public String getPhrase() {
            return phrase;
        }
    }
}