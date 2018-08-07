package kowoof.dogetracker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by shaozheng on 2016/8/12.
 * Modified by kowoof to fit DogeTracker
 */
public class CuteR {
    private static final String TAG = "CuteR";

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private static int[] patternCenters;
    private static final int SCALE_NORMAL_QR = 10;

    public static Bitmap Product(String txt, Bitmap input, boolean colorful, int color){
        Log.d(TAG, "Product start input input.getWidth(): " + input.getWidth() + " input.getHeight(): " + input.getHeight());

        Bitmap QRImage = null;
        try {
            QRImage = encodeAsBitmap(txt);
        } catch (WriterException e) {
            Log.e(TAG, "encodeAsBitmap: " + e);
        }

        if (colorful && color != Color.BLACK) {
            QRImage = replaceColor(QRImage, color);
        }

        int scale = 10; //Reduce time of making qr code, we have fixed scalling
        Bitmap scaledQRImage = Bitmap.createScaledBitmap(QRImage, QRImage.getWidth() * scale, QRImage.getHeight() * scale, false);
        int imageSize;
        Bitmap resizedImage;
        if (input.getWidth() < input.getHeight()) {
            resizedImage = Bitmap.createScaledBitmap(input, scaledQRImage.getWidth() - scale  * 4 * 2, (int)((scaledQRImage.getHeight() - scale  * 4 * 2) * (1.0 * input.getHeight() / input.getWidth())), false);
            imageSize = resizedImage.getWidth();
        } else {
            resizedImage = Bitmap.createScaledBitmap(input, (int)((scaledQRImage.getWidth() - scale  * 4 * 2) * (1.0 * input.getWidth() / input.getHeight())), scaledQRImage.getHeight() - scale  * 4 * 2, false);
            imageSize = resizedImage.getHeight();
        }

        int[][] pattern = new int[scaledQRImage.getWidth() - scale  * 4 * 2][scaledQRImage.getWidth() - scale  * 4 * 2];
        for (int i = 0; i < patternCenters.length; i++) {
            for (int j = 0; j < patternCenters.length; j++) {
                if (!(patternCenters[i] == 6 && patternCenters[j] == patternCenters[patternCenters.length - 1] ||
                        (patternCenters[j] == 6 && patternCenters[i] == patternCenters[patternCenters.length - 1]) ||
                        (patternCenters[i] == 6 && patternCenters[j] == 6))) {
                    int initx = scale * (patternCenters[i] - 2);
                    int inity = scale * (patternCenters[j] - 2);
                    for (int x = initx; x < initx + scale * 5; x++) {
                        for (int y = inity; y < inity + scale * 5; y++) {
                            pattern[x][y] = 1;
                        }
                    }
                }
            }
        }

        Bitmap blackWhite = resizedImage;
        if (!colorful) {
            blackWhite = convertBlackWhiteFull(blackWhite);
        }

        for (int i = 0; i < imageSize; i++) {
            for (int j = 0; j < imageSize; j++) {
                if ((i * 3 / scale) % 3 == 1 && (j * 3 / scale) % 3 == 1) {
                    continue;
                }
                if (i < scale  * 4 * 2 && (j < scale  * 4 * 2 || j > imageSize -(scale  * 4 * 2 + 1))) {
                    continue;
                }
                if (i > imageSize - (scale  * 4 * 2 + 1) && j < scale  * 4 * 2) {
                    continue;
                }

                if (pattern[i][j] == 1) {
                    continue;
                }

                scaledQRImage.setPixel(i + scale  * 4, j + scale  * 4, blackWhite.getPixel(i, j));
            }
        }
        Log.d(TAG, "Product end input scaledQRImage.getWidth(): " + scaledQRImage.getWidth() + " scaledQRImage.getHeight(): " + scaledQRImage.getHeight());
        return scaledQRImage;
    }

    private static Bitmap convertBlackWhiteFull(Bitmap blackWhite) {
        blackWhite = createContrast(blackWhite, 50, 30);
        blackWhite = ConvertToBlackAndWhite(blackWhite);
        blackWhite = convertGreyImgByFloyd2(blackWhite);
        return blackWhite;
    }

    public static Bitmap ProductNormal(String txt, boolean colorful, int color) {
        Bitmap QRImage;
        try {
            QRImage = encodeAsBitmap(txt);
        } catch (WriterException e) {
            Log.e(TAG, "encodeAsBitmap: " + e);
            return null;
        }

        if (colorful && color != Color.BLACK) {
            QRImage = replaceColor(QRImage, color);
        }
        return Bitmap.createScaledBitmap(QRImage, QRImage.getWidth() * SCALE_NORMAL_QR, QRImage.getHeight() * SCALE_NORMAL_QR, false);
    }

    private static Bitmap replaceColor(Bitmap qrBitmap, int color) {
        int [] allpixels = new int [qrBitmap.getHeight()*qrBitmap.getWidth()];

        qrBitmap.getPixels(allpixels, 0, qrBitmap.getWidth(), 0, 0, qrBitmap.getWidth(), qrBitmap.getHeight());

        for(int i = 0; i < allpixels.length; i++)
        {
            if(allpixels[i] == Color.BLACK)
            {
                allpixels[i] = color;
            }
        }

        qrBitmap.setPixels(allpixels, 0, qrBitmap.getWidth(), 0, 0, qrBitmap.getWidth(), qrBitmap.getHeight());
        return qrBitmap;
    }

    private static Bitmap encodeAsBitmap(String txt) throws WriterException {
        return encodeAsBitmap(txt, ErrorCorrectionLevel.M);
    }

    private static Bitmap encodeAsBitmap(String contentsToEncode, ErrorCorrectionLevel level) throws WriterException {
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = new EnumMap(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 0);

        BitMatrix result;
        QRCode qrCode;
        try {
            qrCode = Encoder.encode(contentsToEncode, level, hints);
            patternCenters = qrCode.getVersion().getAlignmentPatternCenters();
            result = renderResult(qrCode, 4);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    // Note that the input matrix uses 0 == white, 1 == black, while the output matrix uses
    // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
    private static BitMatrix renderResult(QRCode code, int quietZone) {
        ByteMatrix input = code.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth + (quietZone * 2);
        int qrHeight = inputHeight + (quietZone * 2);
        int outputWidth = Math.max(0, qrWidth);
        int outputHeight = Math.max(0, qrHeight);
        int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
        // Padding includes both the quiet zone and the extra white pixels to accommodate the requested
        // dimensions. For example, if input is 25x25 the QR will be 33x33 including the quiet zone.
        // If the requested size is 200x160, the multiple will be 4, for a QR of 132x132. These will
        // handle all the padding from 100x100 (the actual QR) up to 200x160.
        int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
        int topPadding = (outputHeight - (inputHeight * multiple)) / 2;
        BitMatrix output = new BitMatrix(outputWidth, outputHeight);
        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
        // Write the contents of this row of the barcode
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                if (input.get(inputX, inputY) == 1) {
                    output.setRegion(outputX, outputY, multiple, multiple);
                }
            }
        }
        return output;
    }

    private static Bitmap ConvertToBlackAndWhite(Bitmap sampleBitmap){
        ColorMatrix bwMatrix =new ColorMatrix();
        bwMatrix.setSaturation(0);
        final ColorMatrixColorFilter colorFilter= new ColorMatrixColorFilter(bwMatrix);
        Bitmap rBitmap = sampleBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Paint paint=new Paint();
        paint.setColorFilter(colorFilter);
        Canvas myCanvas =new Canvas(rBitmap);
        myCanvas.drawBitmap(rBitmap, 0, 0, paint);
        return rBitmap;
    }

    private static Bitmap createContrast(Bitmap src, double value, int brightness) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0) + brightness;
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = Color.green(pixel);
                G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0) + brightness;
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = Color.blue(pixel);
                B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0) + brightness;
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    private static Bitmap convertGreyImgByFloyd2(Bitmap img) {

        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray=new int[height*width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                gray[width*i+j] = grey & 0xFF;
            }
        }

        int e;
        int divide = 16;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int g = gray[width * i + j];
                int newPixel = (g >> 7) * 255;
                e = g - newPixel;
                pixels[width * i + j] = newPixel > 0 ? WHITE : BLACK;
                if (j + 1 < width) {
                    gray[width * i + j + 1] += e * 7 / divide;
                }

                if (j - 1 >= 0 && i + 1 < height) {
                    gray[width * (i + 1) + j - 1] += e * 3 / divide;
                }

                if (i + 1 < height) {
                    gray[width * (i + 1) + j] += e * 5 / divide;
                }

                if (j + 1 < width && i + 1 < height) {
                    gray[width * (i + 1) + j + 1] += e / divide;
                }
            }
        }
        Bitmap mBitmap=Bitmap.createBitmap(width, height, img.getConfig());
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return mBitmap;
    }
}
