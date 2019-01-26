package com.quber.utility;

import android.graphics.Bitmap;

public class ChannelManager {

    /// Create an enumeration to preserve the order of the channels in arrays
    enum Channels {
        CYAN(0),
        MAGENTA(1),
        YELLOW(2);

        private int id;

        // The number of the channel can be accessed by the name "Channels.[COLOUR].id"
        Channels(int id) {
            this.id = id;
        }
    }

    public static Bitmap generateQubeRCode(Bitmap imgC, Bitmap imgM, Bitmap imgY){
        // ## Although supposed to be of the same dimensions, define the width and height of the
        // input bitmaps and take the largest
        int width = Math.max(Math.max(imgC.getWidth(),imgM.getWidth()),imgY.getWidth());
        int height = Math.max(Math.max(imgC.getHeight(),imgM.getHeight()),imgY.getHeight());
        // Create an empty image with a pixels as four-byte object of Alpha Red Green Blue (argb)
        // values; one per one:
        // p = aaaaaaaa rrrrrrrr gggggggg bbbbbbbb
        //           24       16        8        0

        Bitmap cubeR = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        // Cycle through the three images and see if there are black dots.
        for (int atY = 0; atY < height; atY++) {
            for (int atX = 0; atX < width; atX++) {
                // 0xff000000 is the color black
                boolean c = (imgC.getPixel(atX, atY) == 0xff000000);
                boolean y = (imgY.getPixel(atX, atY) == 0xff000000);
                boolean m = (imgM.getPixel(atX, atY) == 0xff000000);

                // Resolve the Red-Green-Blue, Negative, Cyan-Magenta-Yellow channels
                int p = 0xffffffff;

                if (c)
                    p &= 0xff00ffff;
                if (m)
                    p &= 0xffff00ff;
                if (y)
                    p &= 0xffffff00;

                cubeR.setPixel(atX,atY,p);
            }
        }
        return cubeR;
    }

    public static Bitmap[] analyzeQubeRCode(Bitmap srcImg, boolean shiftToBlack) {
        //## Define the used variables
        // Define a Bitmap array of the size of the used channels
        final int CHANNELS = Channels.values().length;
        Bitmap[] img = new Bitmap[CHANNELS];     // a Bitmap array of the output images

        // Get source image width and height
        int width = srcImg.getWidth();
        int height = srcImg.getHeight();


        //## Fill out the output images array with the source for starters
        // to avoid 'java.lang.NullPointerException'

        // Cycle through the channels and define their relevant filenames
        for (int i = 0; i < CHANNELS; i++)
            img[i] = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Cycle through each pixel in the source image and do some processing
        for (int atY = 0; atY < height; atY++) {
            for (int atX = 0; atX < width; atX++) {
                // Get the source pixel
                // A pixel (p) is a four-byte object of Alpha Red Green Blue (argb) values; as
                // follows:
                // p = aaaaaaaa rrrrrrrr gggggggg bbbbbbbb
                //           24       16        8        0
                int p = srcImg.getPixel(atX, atY);

                // Resolve the alpha channel (This is the opacity and we will need this to stay the
                // same all around).
                int a = (p >> 24) & 0xff;

                // Resolve the Cyan-Magenta-Yellow channels
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                int p_ = (a << 24) | (255 - r) << 16 | (255 - g) << 8 | (255 - b);

                int c = (p_ >> 16) & 0xff;
                int m = (p_ >> 8) & 0xff;
                int y = p_ & 0xff;

                int[] pixel = new int[CHANNELS];
                // Rebuild the channels one by one

                //pixel[Channels.SOURCE.id] = (a<<24) | (r<<16) | (g<<8) | b;
                if(shiftToBlack){
                    pixel[Channels.CYAN.id] =       (a << 24) | ~((c << 16) | (c << 8) | c);
                    pixel[Channels.MAGENTA.id] =    (a << 24) | ~((m << 16) | (m << 8) | m);
                    pixel[Channels.YELLOW.id] =     (a << 24) | ~((y << 16) | (y << 8) | y);
                } else {
                    pixel[Channels.CYAN.id] =       (a << 24) | ~(c << 16);
                    pixel[Channels.MAGENTA.id] =    (a << 24) | ~(m << 8);
                    pixel[Channels.YELLOW.id] =     (a << 24) | ~y;
                }

                // Circle through the output images with modified relevant pixel values.
                for (int i = 0; i < CHANNELS; i++)
                    img[i].setPixel(atX, atY, pixel[i]);
            }
        }
        return img;
    }//main() ends here
}
