package org.openmrs.module.billing.util;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.util.ConfigUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class ReceiptGenerator {
	
	private static final Logger LOG = LoggerFactory.getLogger(ReceiptGenerator.class);
	
	private static final String GP_BILL_LOGO_PATH = "billing.receipt.logoPath";
	
	//TODO: Try to clean this up more
	public static byte[] createBillReceipt(Bill bill) {
		NumberFormat nf = NumberFormat.getCurrencyInstance(Context.getLocale());
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
		        .withLocale(Context.getLocale());
		
		Patient patient = bill.getPatient();
		String fullName = patient.getPersonName().getFullName();
		String gender = patient.getGender() != null ? patient.getGender() : "";
		String dob = patient.getBirthdate() != null ? dateFormatter.format(patient.getBirthdate().toInstant()) : "";
		
		/**
		 * https://kb.itextpdf.com/home/it7kb/faq/how-to-set-the-page-size-to-envelope-size-with-landscape-orientation
		 * page size: 3.5inch length, 1.1 inch height 1mm = 0.0394 inch length = 450mm = 17.7165 inch =
		 * 127.5588 points height = 300mm = 11.811 inch = 85.0392 points The measurement system in PDF
		 * doesn't use inches, but user units. By default, 1 user unit = 1 point, and 1 inch = 72 points.
		 * Thermal printer: 4 x 10 inches paper 4 inches = 4 x 72 = 288 5 inches = 10 x 72 = 720
		 */
		int FONT_SIZE_12 = 12;
		Rectangle thermalPrinterPageSize = new Rectangle(288, 720);
		
		PdfFont timesRoman;
		PdfFont courierBold;
		PdfFont helvetica;
		PdfFont helveticaBold;
		try {
			timesRoman = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
			courierBold = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD);
			helvetica = PdfFontFactory.createFont(StandardFonts.HELVETICA);
			helveticaBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
			
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		PdfFont headerSectionFont = helveticaBold;
		PdfFont billItemSectionFont = helvetica;
		PdfFont footerSectionFont = courierBold;
		URL logoUrl = null;
		
		String logoPath = ConfigUtil.getGlobalProperty(GP_BILL_LOGO_PATH);
		if (StringUtils.isNotBlank(logoPath)) {
			File file = new File(logoPath.trim());
			if (!file.isAbsolute()) {
				file = new File(OpenmrsUtil.getApplicationDataDirectory(), logoPath.trim());
			}
			
			if (file.exists()) {
				try {
					logoUrl = file.getAbsoluteFile().toURI().toURL();
				}
				catch (MalformedURLException e) {
					LOG.error("Error Loading file: {}", file.getAbsoluteFile(), e);
				}
			}
		}
		
		if (logoUrl == null) {
			logoUrl = OpenmrsClassLoader.getInstance().getResource("img/openmrs-logo.png");
		}
		
		Image logoImage = null;
		if (logoUrl != null) {
			logoImage = new Image(ImageDataFactory.create(logoUrl));
			logoImage.scaleToFit(80, 80);
		}
		Paragraph divider = new Paragraph("------------------------------------------------------------------");
		Text billDateLabel = new Text(Utils.getSimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(bill.getDateCreated()));
		
		Paragraph logoSection = null;
		if (logoImage != null) {
			logoSection = new Paragraph();
			logoSection.setFontSize(14);
			logoSection.add(logoImage).add("\n");
			logoSection.setTextAlignment(TextAlignment.CENTER);
			logoSection.setFont(timesRoman).setBold();
		}
		
		float[] headerColWidth = { 2f, 7f };
		Table receiptHeader = new Table(headerColWidth);
		receiptHeader.setWidth(UnitValue.createPercentValue(100f));
		
		receiptHeader.addCell(new Paragraph("Date:")).setFontSize(FONT_SIZE_12).setTextAlignment(TextAlignment.LEFT)
		        .setFont(headerSectionFont);
		receiptHeader.addCell(new Paragraph(billDateLabel.getText())).setFontSize(FONT_SIZE_12)
		        .setTextAlignment(TextAlignment.LEFT).setFont(helvetica);
		
		receiptHeader.addCell(new Paragraph("Receipt No:")).setFontSize(FONT_SIZE_12).setTextAlignment(TextAlignment.LEFT)
		        .setFont(headerSectionFont);
		receiptHeader.addCell(new Paragraph(bill.getReceiptNumber())).setFontSize(FONT_SIZE_12)
		        .setTextAlignment(TextAlignment.LEFT).setFont(helvetica);
		
		receiptHeader.addCell(new Paragraph("Patient:")).setFontSize(FONT_SIZE_12).setTextAlignment(TextAlignment.LEFT)
		        .setFont(headerSectionFont);
		receiptHeader.addCell(new Paragraph(WordUtils.capitalizeFully(fullName))).setFontSize(FONT_SIZE_12)
		        .setTextAlignment(TextAlignment.LEFT).setFont(helvetica);
		
		receiptHeader.addCell(new Paragraph("Gender:")).setFontSize(FONT_SIZE_12).setTextAlignment(TextAlignment.LEFT)
		        .setFont(headerSectionFont);
		receiptHeader.addCell(new Paragraph(WordUtils.capitalizeFully(gender))).setFontSize(FONT_SIZE_12)
		        .setTextAlignment(TextAlignment.LEFT).setFont(helvetica);
		
		receiptHeader.addCell(new Paragraph("Date of Birth:")).setFontSize(FONT_SIZE_12).setTextAlignment(TextAlignment.LEFT)
		        .setFont(headerSectionFont);
		receiptHeader.addCell(new Paragraph(WordUtils.capitalizeFully(dob))).setFontSize(FONT_SIZE_12)
		        .setTextAlignment(TextAlignment.LEFT).setFont(helvetica);
		
		float[] columnWidths = { 1f, 5f, 2f, 2f };
		Table billLineItemstable = new Table(columnWidths);
		billLineItemstable.setBorder(Border.NO_BORDER);
		billLineItemstable.setWidth(UnitValue.createPercentValue(100f));
		
		billLineItemstable.addCell(new Paragraph("Qty").setTextAlignment(TextAlignment.LEFT)).setFontSize(FONT_SIZE_12)
		        .setTextAlignment(TextAlignment.LEFT);
		billLineItemstable.addCell(new Paragraph("Item").setTextAlignment(TextAlignment.LEFT)).setFontSize(FONT_SIZE_12)
		        .setTextAlignment(TextAlignment.LEFT);
		billLineItemstable.addCell(new Paragraph("Price")).setFontSize(FONT_SIZE_12).setTextAlignment(TextAlignment.RIGHT);
		billLineItemstable.addCell(new Paragraph("Total")).setFontSize(FONT_SIZE_12).setTextAlignment(TextAlignment.RIGHT);
		
		for (BillLineItem item : bill.getLineItems()) {
			if (item.getVoided()) {
				continue;
			}
			
			addBillLineItem(item, billLineItemstable, billItemSectionFont, nf);
		}
		
		float[] totalColWidth = { 1f, 5f, 2f, 2f };
		Table totalsSection = new Table(totalColWidth);
		totalsSection.setWidth(UnitValue.createPercentValue(100f));
		
		totalsSection.addCell(new Paragraph(" "));
		totalsSection.addCell(new Paragraph(" "));
		totalsSection.addCell(new Paragraph("Total")).setFontSize(10).setTextAlignment(TextAlignment.RIGHT)
		        .setFont(helvetica).setBold();
		totalsSection.addCell(new Paragraph(nf.format(bill.getTotal()))).setFontSize(10)
		        .setTextAlignment(TextAlignment.RIGHT).setFont(helvetica).setBold();
		
		setInnerCellBorder(receiptHeader, Border.NO_BORDER);
		setInnerCellBorder(billLineItemstable, Border.NO_BORDER);
		
		float[] paymentColWidth = { 1f, 5f, 2f, 2f };
		Table paymentSection = new Table(paymentColWidth);
		paymentSection.setWidth(UnitValue.createPercentValue(100f));
		paymentSection.addCell(new Paragraph("  "));
		paymentSection.addCell(new Paragraph("  "));
		paymentSection.addCell(new Paragraph("Payment").setTextAlignment(TextAlignment.RIGHT).setBold());
		paymentSection.addCell(new Paragraph(""));
		// append payment rows
		for (Payment payment : bill.getPayments()) {
			paymentSection.addCell(new Paragraph(" "));
			paymentSection.addCell(new Paragraph(" "));
			paymentSection.addCell(new Paragraph(payment.getInstanceType().getName()).setTextAlignment(TextAlignment.RIGHT))
			        .setFontSize(10).setFont(helvetica);
			paymentSection
			        .addCell(new Paragraph(nf.format(payment.getAmountTendered())).setTextAlignment(TextAlignment.RIGHT))
			        .setFontSize(10).setFont(helvetica);
		}
		
		float[] amountDueColWidth = { 1f, 5f, 2f, 2f };
		Table amountDueSection = new Table(amountDueColWidth);
		amountDueSection.setWidth(UnitValue.createPercentValue(100f));
		
		amountDueSection.addCell(new Paragraph(" "));
		amountDueSection.addCell(new Paragraph(" "));
		
		amountDueSection.addCell(new Paragraph("Due Amount")).setFontSize(10).setTextAlignment(TextAlignment.RIGHT)
		        .setFont(helvetica).setBold();
		BigDecimal dueAmount = bill.getTotal().subtract(bill.getTotalPayments());
		if (dueAmount.compareTo(BigDecimal.ZERO) > 0) {
			amountDueSection.addCell(new Paragraph(nf.format(dueAmount))).setFontSize(10)
			        .setTextAlignment(TextAlignment.RIGHT).setFont(helvetica).setBold();
		} else {
			amountDueSection.addCell(new Paragraph("0.00")).setFontSize(10).setTextAlignment(TextAlignment.RIGHT)
			        .setFont(helvetica).setBold();
		}
		setInnerCellBorder(paymentSection, Border.NO_BORDER);
		setInnerCellBorder(amountDueSection, Border.NO_BORDER);
		setInnerCellBorder(totalsSection, Border.NO_BORDER);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(bos));
		        Document doc = new Document(pdfDoc, new PageSize(thermalPrinterPageSize))) {
			doc.setMargins(6, 12, 2, 12);
			if (logoSection != null) {
				doc.add(logoSection);
			}
			//doc.add(addressSection);
			doc.add(receiptHeader);
			doc.add(divider);
			doc.add(billLineItemstable);
			doc.add(divider);
			doc.add(totalsSection);
			doc.add(divider);
			doc.add(paymentSection);
			doc.add(divider);
			doc.add(amountDueSection);
			doc.add(divider);
			doc.add(new Paragraph("You were served by " + bill.getCashier().getName()).setFont(footerSectionFont)
			        .setFontSize(8).setTextAlignment(TextAlignment.CENTER));
		}
		catch (Exception e) {
			LOG.error("Exception caught while writing PDF to stream", e);
			return bos.toByteArray();
		}
		
		return bos.toByteArray();
	}
	
	private static void setInnerCellBorder(Table table, Border border) {
		for (IElement child : table.getChildren()) {
			if (child instanceof Cell) {
				((Cell) child).setBorder(border);
			}
		}
	}
	
	private static void addBillLineItem(BillLineItem item, Table table, PdfFont font, NumberFormat nf) {
		String itemName = "";
		if (item.getItem() != null) {
			itemName = item.getItem().getDrug().getName();
		} else if (item.getBillableService() != null) {
			itemName = item.getBillableService().getName();
		}
		addFormattedCell(table, item.getQuantity().toString(), font, TextAlignment.LEFT);
		addFormattedCell(table, itemName, font, TextAlignment.LEFT);
		addFormattedCell(table, nf.format(item.getPrice()), font, TextAlignment.RIGHT);
		addFormattedCell(table, nf.format(item.getTotal()), font, TextAlignment.RIGHT);
	}
	
	private static void addFormattedCell(Table table, String cellValue, PdfFont font, TextAlignment alignment) {
		table.addCell(new Paragraph(cellValue).setTextAlignment(alignment)).setFontSize(12).setTextAlignment(alignment)
		        .setBorder(Border.NO_BORDER).setFont(font);
	}
}
