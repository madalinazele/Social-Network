package socialnetwork.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;

public class PdfReports {
    private final String FILE;

    public PdfReports(String FILE) {
        this.FILE = FILE;
    }

    public void addData(java.util.List<String> data, String title) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(FILE));
            document.open();
            Font f = new Font();
            f.setStyle(Font.BOLD);
            f.setSize(14);
            Paragraph paragraph = new Paragraph(title + "\n", f);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);

            f = new Font();
            f.setSize(9);
            paragraph = new Paragraph();
            for (String s : data) {
                paragraph.add(s);
            }
            document.add(paragraph);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
