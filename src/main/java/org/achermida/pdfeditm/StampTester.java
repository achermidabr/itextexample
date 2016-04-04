/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.achermida.pdfeditm;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.BarcodeEAN;
import com.itextpdf.text.pdf.BarcodePDF417;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.SimpleBookmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import static java.util.Collections.copy;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class StampTester {

    private static final DateFormat form = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.SHORT);
    private static final String SRC = "orig.pdf";
    private static final String DEST = "dest.pdf";

    private static final Font FONT_BIG_BOLD = new Font(FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font FONT_SMALL_BOLD = new Font(FontFamily.HELVETICA, 6, Font.BOLD);
    private static final Font FONT_REGULAR = new Font(FontFamily.HELVETICA, 8);
    
    public static void main(String[] args) throws IOException, DocumentException {
        File file = new File(DEST);
//        file.getParentFile().mkdirs();
        new StampTester().manipulatePdf(SRC, DEST);
    }

    public void manipulatePdf(String src, String dest) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));

        HashMap<String, Object> outline = new HashMap<String, Object>();
        outline.put("Title", "Segundo capitulo");
        outline.put("Action", "GoTo");
        outline.put("Page", String.format("%d Fit", 10));
        merge(stamper, reader,DEST,outline);
        
        
        Phrase linha1 = new Phrase(form.format(new Date()), FONT_SMALL_BOLD);
        Phrase linha2 = new Phrase("Assinado digitalmente por achermida", FONT_REGULAR);

        float x1 = reader.getPageSize(1).getLeft(reader.getPageSize(1).getWidth() - 13);
        float x2 = reader.getPageSize(1).getLeft(reader.getPageSize(1).getWidth() - 5);
        float y1 = reader.getPageSize(1).getTop(reader.getPageSize(1).getHeight() / 2);
        ColumnText.showTextAligned(stamper.getOverContent(1), Element.ALIGN_CENTER, linha1, x1, y1, 90);
        ColumnText.showTextAligned(stamper.getOverContent(1), Element.ALIGN_CENTER, linha2, x2, y1, 90);
        Image img = null;
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            //img = createBarCodeEan(stamper.getOverContent(i), String.valueOf("1200000" + i));
            img = createBarCodeEan2(stamper.getOverContent(i), "Página " + i, String.format("%08d", i));
            img.setRotationDegrees(90);
            stamper.getOverContent(i).addImage(img, 60, 0, 0, 20, reader.getPageSize(i).getWidth() - 120, reader.getPageSize(i).getHeight() - 30);
        }
        stamper.close();
        reader.close();
    }

    public Image createBarCodeEan(PdfContentByte cb, String text) {
        BarcodeEAN codeEAN = new BarcodeEAN();
        codeEAN.setCodeType(Barcode.EAN8);
        codeEAN.setCode(text);
        return codeEAN.createImageWithBarcode(cb, BaseColor.BLACK, BaseColor.BLACK);
    }

    public Image createBarCodeEan2(PdfContentByte cb, String text, String number) throws BadElementException {
        BarcodeEAN barcode = new BarcodeEAN();
        barcode.setCodeType(Barcode.EAN8);
        barcode.setCode(number);
        Rectangle rect = barcode.getBarcodeSize();
        PdfTemplate template = cb.createTemplate(rect.getWidth(), rect.getHeight() + 10);
        ColumnText.showTextAligned(template, Element.ALIGN_LEFT,
                new Phrase(text, FONT_REGULAR), 0, rect.getHeight() + 2, 0);
        barcode.placeBarcode(template, BaseColor.BLACK, BaseColor.BLACK);
        return Image.getInstance(template);
    }

    public void merge(PdfStamper origDoc, PdfReader doc2MergeReader, String outDoc, HashMap<String,Object> entry) throws FileNotFoundException, DocumentException, IOException {
        origDoc.insertPage(4, doc2MergeReader.getPageSize(1));
        PdfContentByte cb = origDoc.getOverContent(4);
        cb.addTemplate(origDoc.getImportedPage(doc2MergeReader, 1), 0, 0);
        List<HashMap<String, Object>> outlines = SimpleBookmark.getBookmark(origDoc.getReader());
        updateOutline(outlines, entry, 4);
        ArrayList<HashMap<String,Object>> link = new ArrayList<>();
        origDoc.setOutlines(outlines);
    }

    public Image createBarcode(PdfContentByte cb, String text, float mh, float mw) throws BadElementException {
//        BarcodePDF417 pf = new BarcodePDF417();
        BarcodePDF417 pf = new BarcodePDF417();
        pf.setText(text);
        Rectangle size = pf.getBarcodeSize();
        PdfTemplate template = cb.createTemplate(mw * size.getWidth(), mh * size.getHeight());
        pf.placeBarcode(template, BaseColor.BLACK, mh, mw);
        return Image.getInstance(template);
    }
    public boolean updateOutline(List<HashMap<String, Object>> outlines, HashMap<String, Object> entry, int p) {
//        int index = 0;
//        for (HashMap<String, Object> outline : outlines) {
//            Object kids = outline.get("Kids");
//            if (kids != null) {
//                updateOutline((List<HashMap<String, Object>>)kids, entry, p);
//            }
//            else {
//                if (p < getPage(outline)) {
//                    outlines.add(index, entry);
//                    return true;
//                }
//                index++;
//            }
//        }
        return false;
    }
 
    public int getPage(HashMap<String, Object> outline) {
        Object page = outline.get("Page");
        if (page == null) return -1;
        String p = page.toString().substring(0, page.toString().indexOf(" "));
        return Integer.parseInt(p);
    }
}
