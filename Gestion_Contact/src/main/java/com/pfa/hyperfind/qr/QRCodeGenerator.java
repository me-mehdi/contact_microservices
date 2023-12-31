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
import com.pfa.hyperfind.entity.ContactDetails;

public class QRCodeGenerator {

    public static void generateQRCode(ContactDetails contact) throws WriterException, IOException {
       String qrCodePath = "src/main/resources/static/img/";
       // String qrCodePath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "QR_USER" + File.separator;

        String qrCodeName = qrCodePath+contact.getId()+".png";
        var qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                 "Email: "+contact.getEmail()+ "\n"+
                         "Firstname: "+contact.getName()+ "\n"+
                         "Phone: "+contact.getPhone()+ "\n"+
                         "Gender: "+contact.getGender(), BarcodeFormat.QR_CODE, 400, 400);
        Path path = FileSystems.getDefault().getPath(qrCodeName);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

    }
}
