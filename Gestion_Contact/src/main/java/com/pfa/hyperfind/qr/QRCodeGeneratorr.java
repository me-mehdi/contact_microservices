package com.pfa.hyperfind.qr;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.pfa.hyperfind.entity.User;

public class QRCodeGeneratorr {

    public static void generateQRCode(User user) throws WriterException, IOException {
        String qrCodePath = "src/main/resources/static/imgu/";
        //String qrCodePath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "QR_USER" + File.separator;

        String qrCodeName = qrCodePath+user.getId()+".png";
        var qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                 "Email: "+user.getEmail()+ "\n"+
                         "Firstname: "+user.getfName()+ "\n"+
                         "Phone: "+user.getPhone()+ "\n"+
                         "USername: "+user.getUsername(), BarcodeFormat.QR_CODE, 400, 400);
        Path path = FileSystems.getDefault().getPath(qrCodeName);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

    }
}