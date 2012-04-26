package com.paymium.instawallet.qrcode;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QrCode 
{
	public static Bitmap generateQrCode(String data, int width, int height) 
	{
		BitMatrix bitMatrix;
		Bitmap bitmap = null;

		try 
		{
			bitMatrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, width, height);
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

			for (int x = 0; x < bitMatrix.getWidth(); ++x) 
			{
				for (int y = 0; y < bitMatrix.getHeight(); ++y) 
				{
					int color = bitMatrix.get(x, y) ? 0xFF000000 : 0x00FFFFFF;
					bitmap.setPixel(x, y, color);
				}
			}
		} 
		catch (WriterException e) 
		{
			// Not much we can do...
		}

		return (bitmap);
	}
}