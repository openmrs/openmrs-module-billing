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
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.openmrs.Patient;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.util.CashierModuleConstants;
import org.openmrs.util.ConfigUtil;
import org.openmrs.util.LocaleUtility;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class ReceiptGenerator {
	
	private static final Logger LOG = LoggerFactory.getLogger(ReceiptGenerator.class);
	
	public static byte[] createBillReceipt(Bill bill) {
		NumberFormat nf = buildNumberFormat();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
		        .withLocale(LocaleUtility.getDefaultLocale());
		
		ReceiptFonts receiptFonts = loadFonts();
		Image logoImage = loadLogoImage();
		
		/**
		 * https://kb.itextpdf.com/home/it7kb/faq/how-to-set-the-page-size-to-envelope-size-with-landscape-orientation
		 * page size: 3.5inch length, 1.1 inch height 1mm = 0.0394 inch length = 450mm = 17.7165 inch =
		 * 127.5588 points height = 300mm = 11.811 inch = 85.0392 points The measurement system in PDF
		 * doesn't use inches, but user units. By default, 1 user unit = 1 point, and 1 inch = 72 points.
		 * Thermal printer: 4 x 10 inches paper 4 inches = 4 x 72 = 288 5 inches = 10 x 72 = 720
		 */
		Rectangle thermalPrinterPageSize = new Rectangle(BillingPdfConstants.PAGE_WIDTH_PTS,
		        BillingPdfConstants.PAGE_HEIGHT_PTS);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(bos));
		        Document doc = new Document(pdfDoc, new PageSize(thermalPrinterPageSize))) {
			doc.setMargins(BillingPdfConstants.BILLING_RECEIPT_PDF_TOP_MARGIN,
			    BillingPdfConstants.BILLING_RECEIPT_PDF_RIGHT_MARGIN, BillingPdfConstants.BILLING_RECEIPT_PDF_BOTTOM_MARGIN,
			    BillingPdfConstants.BILLING_RECEIPT_PDF_LEFT_MARGIN);
			doc.add(buildLogoSection(logoImage, receiptFonts));
			//doc.add(addressSection);
			doc.add(buildReceiptHeader(bill, dateFormatter, receiptFonts));
			doc.add(divider());
			doc.add(buildLineItemsTable(bill, nf, receiptFonts));
			doc.add(divider());
			doc.add(buildTotalsSection(bill, nf, receiptFonts));
			doc.add(divider());
			doc.add(buildPaymentSection(bill, nf, receiptFonts));
			doc.add(divider());
			doc.add(buildAmountDueSection(bill, nf, receiptFonts));
			doc.add(divider());
			doc.add(buildFooter(bill, receiptFonts));
		}
		catch (Exception e) {
			LOG.error("Exception caught while writing PDF to stream", e);
			return bos.toByteArray();
		}
		
		return bos.toByteArray();
	}
	
	private static NumberFormat buildNumberFormat() {
		NumberFormat nf = NumberFormat.getCurrencyInstance(LocaleUtility.getDefaultLocale());
		String currencySymbol = ConfigUtil.getGlobalProperty(CashierModuleConstants.GLOBAL_PROPERTY_BILLING_CURRENCY);
		if (StringUtils.isNotBlank(currencySymbol) && nf instanceof DecimalFormat) {
			DecimalFormat df = (DecimalFormat) nf;
			DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
			symbols.setCurrencySymbol(currencySymbol.trim());
			df.setDecimalFormatSymbols(symbols);
		}
		return nf;
	}
	
	private static Paragraph buildLogoSection(Image logoImage, ReceiptFonts receiptFonts) {
		Paragraph logoSection = null;
		if (logoImage != null) {
			logoSection = new Paragraph();
			logoSection.setFontSize(14);
			logoSection.add(logoImage).add("\n");
			logoSection.setTextAlignment(TextAlignment.CENTER);
			logoSection.setFont(receiptFonts.logoSectionFont).setBold();
		}
		return logoSection;
	}
	
	private static Table buildReceiptHeader(Bill bill, DateTimeFormatter dateFormatter, ReceiptFonts receiptFonts) {
		Table receiptHeader = new Table(BillingPdfConstants.HEADER_COL_WIDTHS);
		receiptHeader.setWidth(UnitValue.createPercentValue(100f));
		
		String billDate = Utils.getSimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(bill.getDateCreated());
		Patient patient = bill.getPatient();
		String dob = patient.getBirthdate() != null
		        ? dateFormatter.format(patient.getBirthdate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
		        : "";
		
		addHeaderRow(receiptHeader, "Date", billDate, receiptFonts);
		addHeaderRow(receiptHeader, "Receipt No", bill.getReceiptNumber(), receiptFonts);
		addHeaderRow(receiptHeader, "Patient", WordUtils.capitalizeFully(patient.getPersonName().getFullName()),
		    receiptFonts);
		addHeaderRow(receiptHeader, "Gender", WordUtils.capitalizeFully(nullSafe(patient.getGender())), receiptFonts);
		addHeaderRow(receiptHeader, "Date of Birth", WordUtils.capitalizeFully(dob), receiptFonts);
		
		setInnerCellBorder(receiptHeader, Border.NO_BORDER);
		return receiptHeader;
	}
	
	private static Table buildLineItemsTable(Bill bill, NumberFormat nf, ReceiptFonts receiptFonts) {
		Table billLineItemstable = new Table(BillingPdfConstants.BILL_COL_WIDTHS);
		billLineItemstable.setWidth(UnitValue.createPercentValue(100f));
		billLineItemstable.setBorder(Border.NO_BORDER);
		
		addFormattedCell(billLineItemstable, "Qty", receiptFonts.billItemSectionFont, TextAlignment.LEFT);
		addFormattedCell(billLineItemstable, "Item", receiptFonts.billItemSectionFont, TextAlignment.LEFT);
		addFormattedCell(billLineItemstable, "Price", receiptFonts.billItemSectionFont, TextAlignment.RIGHT);
		addFormattedCell(billLineItemstable, "Total", receiptFonts.billItemSectionFont, TextAlignment.RIGHT);
		
		for (BillLineItem item : bill.getLineItems()) {
			if (!item.getVoided()) {
				addBillLineItemRow(billLineItemstable, item, receiptFonts.billItemSectionFont, nf);
			}
		}
		
		setInnerCellBorder(billLineItemstable, Border.NO_BORDER);
		return billLineItemstable;
	}
	
	private static Table buildTotalsSection(Bill bill, NumberFormat nf, ReceiptFonts receiptFonts) {
		Table totalsSection = new Table(BillingPdfConstants.TOTALS_COL_WIDTHS);
		totalsSection.setWidth(UnitValue.createPercentValue(100f));
		
		totalsSection.addCell(blankCell());
		totalsSection.addCell(blankCell());
		totalsSection.addCell(new Paragraph("Total").setTextAlignment(TextAlignment.RIGHT))
		        .setFontSize(BillingPdfConstants.FONT_SIZE_SMALL).setFont(receiptFonts.billItemSectionFont).setBold();
		totalsSection.addCell(new Paragraph(nf.format(bill.getTotal())).setTextAlignment(TextAlignment.RIGHT))
		        .setFontSize(BillingPdfConstants.FONT_SIZE_SMALL).setFont(receiptFonts.billItemSectionFont).setBold();
		
		setInnerCellBorder(totalsSection, Border.NO_BORDER);
		return totalsSection;
	}
	
	private static Table buildPaymentSection(Bill bill, NumberFormat nf, ReceiptFonts receiptFonts) {
		Table paymentSection = new Table(BillingPdfConstants.PAYMENT_COL_WIDTHS);
		paymentSection.setWidth(UnitValue.createPercentValue(100f));
		
		paymentSection.addCell(blankCell());
		paymentSection.addCell(blankCell());
		paymentSection.addCell(new Paragraph("Payment").setTextAlignment(TextAlignment.RIGHT).setBold());
		paymentSection.addCell(blankCell());
		
		for (Payment payment : bill.getPayments()) {
			paymentSection.addCell(blankCell());
			paymentSection.addCell(blankCell());
			paymentSection.addCell(new Paragraph(payment.getInstanceType().getName()).setTextAlignment(TextAlignment.RIGHT))
			        .setFontSize(BillingPdfConstants.FONT_SIZE_SMALL).setFont(receiptFonts.billItemSectionFont);
			paymentSection
			        .addCell(new Paragraph(nf.format(payment.getAmountTendered())).setTextAlignment(TextAlignment.RIGHT))
			        .setFontSize(BillingPdfConstants.FONT_SIZE_SMALL).setFont(receiptFonts.billItemSectionFont);
		}
		
		setInnerCellBorder(paymentSection, Border.NO_BORDER);
		return paymentSection;
	}
	
	private static Table buildAmountDueSection(Bill bill, NumberFormat nf, ReceiptFonts receiptFonts) {
		Table amountDueSection = new Table(BillingPdfConstants.PAYMENT_COL_WIDTHS);
		amountDueSection.setWidth(UnitValue.createPercentValue(100f));
		
		BigDecimal dueAmount = bill.getTotal().subtract(bill.getTotalPayments());
		String dueFormatted = dueAmount.compareTo(BigDecimal.ZERO) > 0 ? nf.format(dueAmount) : "0.00";
		
		amountDueSection.addCell(blankCell());
		amountDueSection.addCell(blankCell());
		amountDueSection.addCell(new Paragraph("Due Amount").setTextAlignment(TextAlignment.RIGHT))
		        .setFontSize(BillingPdfConstants.FONT_SIZE_SMALL).setFont(receiptFonts.billItemSectionFont).setBold();
		amountDueSection.addCell(new Paragraph(dueFormatted).setTextAlignment(TextAlignment.RIGHT))
		        .setFontSize(BillingPdfConstants.FONT_SIZE_SMALL).setFont(receiptFonts.billItemSectionFont).setBold();
		
		setInnerCellBorder(amountDueSection, Border.NO_BORDER);
		return amountDueSection;
	}
	
	private static Paragraph buildFooter(Bill bill, ReceiptFonts receiptFonts) {
		return new Paragraph("You were served by " + bill.getCashier().getName()).setFont(receiptFonts.footerSectionFont)
		        .setFontSize(BillingPdfConstants.FONT_SIZE_FOOTER).setTextAlignment(TextAlignment.CENTER);
	}
	
	private static void addHeaderRow(Table receiptHeader, String label, String value, ReceiptFonts receiptFonts) {
		receiptHeader.addCell(new Paragraph(label)).setFontSize(BillingPdfConstants.FONT_SIZE_NORMAL)
		        .setTextAlignment(TextAlignment.LEFT).setFont(receiptFonts.headerSectionFont);
		receiptHeader.addCell(new Paragraph(value)).setFontSize(BillingPdfConstants.FONT_SIZE_NORMAL)
		        .setTextAlignment(TextAlignment.LEFT).setFont(receiptFonts.billItemSectionFont);
	}
	
	private static void addBillLineItemRow(Table billLineItemstable, BillLineItem item, PdfFont billItemSectionFont,
	        NumberFormat nf) {
		String itemName = resolveItemName(item);
		addFormattedCell(billLineItemstable, item.getQuantity().toString(), billItemSectionFont, TextAlignment.LEFT);
		addFormattedCell(billLineItemstable, itemName, billItemSectionFont, TextAlignment.LEFT);
		addFormattedCell(billLineItemstable, nf.format(item.getPrice()), billItemSectionFont, TextAlignment.RIGHT);
		addFormattedCell(billLineItemstable, nf.format(item.getTotal()), billItemSectionFont, TextAlignment.RIGHT);
	}
	
	private static Paragraph divider() {
		return new Paragraph(BillingPdfConstants.DIVIDER);
	}
	
	private static Paragraph blankCell() {
		return new Paragraph(" ");
	}
	
	private static String resolveItemName(BillLineItem item) {
		if (item.getItem() != null && item.getItem().getDrug() != null) {
			return item.getItem().getDrug().getName();
		}
		if (item.getBillableService() != null) {
			return item.getBillableService().getName();
		}
		return "";
	}
	
	private static String nullSafe(String value) {
		return value != null ? value : "";
	}
	
	private static Image loadLogoImage() {
		URL logoUrl = resolveLogoUrl();
		if (logoUrl == null) {
			return null;
		}
		Image logoImage = new Image(ImageDataFactory.create(logoUrl));
		logoImage.scaleToFit(80, 80);
		return logoImage;
	}
	
	private static URL resolveLogoUrl() {
		String logoPath = ConfigUtil.getGlobalProperty(BillingPdfConstants.GP_BILL_LOGO_PATH);
		if (StringUtils.isNotBlank(logoPath)) {
			File file = new File(logoPath.trim());
			if (!file.isAbsolute()) {
				file = new File(OpenmrsUtil.getApplicationDataDirectory(), logoPath.trim());
			}
			if (file.exists()) {
				try {
					return file.getAbsoluteFile().toURI().toURL();
				}
				catch (MalformedURLException e) {
					LOG.error("Invalid logo path '{}': {}", file.getAbsolutePath(), e.getMessage());
				}
			}
		}
		return OpenmrsClassLoader.getInstance().getResource("img/openmrs-logo.png");
	}
	
	private static ReceiptFonts loadFonts() {
		try {
			return new ReceiptFonts(PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN),
			        PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD),
			        PdfFontFactory.createFont(StandardFonts.HELVETICA),
			        PdfFontFactory.createFont(StandardFonts.COURIER_BOLD));
		}
		catch (IOException e) {
			throw new IllegalStateException("Failed to load PDF fonts", e);
		}
	}
	
	private static void setInnerCellBorder(Table table, Border border) {
		for (IElement child : table.getChildren()) {
			if (child instanceof Cell) {
				((Cell) child).setBorder(border);
			}
		}
	}
	
	private static void addFormattedCell(Table table, String cellValue, PdfFont font, TextAlignment alignment) {
		table.addCell(new Paragraph(cellValue).setTextAlignment(alignment)).setFontSize(12).setTextAlignment(alignment)
		        .setBorder(Border.NO_BORDER).setFont(font);
	}
	
	private static final class ReceiptFonts {
		
		final PdfFont logoSectionFont;
		
		final PdfFont headerSectionFont;
		
		final PdfFont billItemSectionFont;
		
		final PdfFont footerSectionFont;
		
		ReceiptFonts(PdfFont logoSectionFont, PdfFont headerSectionFont, PdfFont billItemSectionFont,
		    PdfFont footerSectionFont) {
			this.logoSectionFont = logoSectionFont;
			this.headerSectionFont = headerSectionFont;
			this.billItemSectionFont = billItemSectionFont;
			this.footerSectionFont = footerSectionFont;
		}
	}
}
