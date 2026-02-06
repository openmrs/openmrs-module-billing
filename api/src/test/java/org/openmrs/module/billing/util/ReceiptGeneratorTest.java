package org.openmrs.module.billing.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.util.CashierModuleConstants;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.openmrs.util.LocaleUtility;

public class ReceiptGeneratorTest extends BaseModuleContextSensitiveTest {
	
	private PatientService patientService;
	
	private ProviderService providerService;
	
	@BeforeEach
	public void setUp() {
		patientService = Context.getPatientService();
		providerService = Context.getProviderService();
		executeDataSet(TestConstants.CORE_DATASET2);
	}
	
	@Test
	public void createBillReceipt_shouldUseDefaultLocaleCurrencyWhenGlobalPropertyNotSet() throws Exception {
		Bill bill = createTestBill();
		
		byte[] pdfBytes = ReceiptGenerator.createBillReceipt(bill);
		
		assertNotNull(pdfBytes);
		assertTrue(pdfBytes.length > 0);
		
		String pdfText = extractTextFromPdf(pdfBytes);
		Locale defaultLocale = LocaleUtility.getDefaultLocale();
		String expectedSymbol = getCurrencySymbol(defaultLocale);
		assertTrue(pdfText.contains(expectedSymbol),
		    "PDF should contain default locale currency symbol (" + expectedSymbol + ") but was: " + pdfText);
	}
	
	@Test
	public void createBillReceipt_shouldUseDefaultLocaleCurrencyWhenGlobalPropertyIsBlank() throws Exception {
		Context.getAdministrationService().setGlobalProperty(CashierModuleConstants.GLOBAL_PROPERTY_BILLING_CURRENCY, "   ");
		
		Bill bill = createTestBill();
		
		byte[] pdfBytes = ReceiptGenerator.createBillReceipt(bill);
		
		assertNotNull(pdfBytes);
		String pdfText = extractTextFromPdf(pdfBytes);
		Locale defaultLocale = LocaleUtility.getDefaultLocale();
		String expectedSymbol = getCurrencySymbol(defaultLocale);
		assertTrue(pdfText.contains(expectedSymbol),
		    "PDF should fall back to default locale currency symbol (" + expectedSymbol + ") when property is blank");
	}
	
	@Test
	public void createBillReceipt_shouldUseConfiguredCurrencyWhenGlobalPropertyIsSet() throws Exception {
		Context.getAdministrationService().setGlobalProperty(CashierModuleConstants.GLOBAL_PROPERTY_BILLING_CURRENCY, "KES");
		
		Bill bill = createTestBill();
		
		byte[] pdfBytes = ReceiptGenerator.createBillReceipt(bill);
		
		assertNotNull(pdfBytes);
		String pdfText = extractTextFromPdf(pdfBytes);
		assertTrue(pdfText.contains("KES"), "PDF should contain configured currency symbol KES but was: " + pdfText);
	}
	
	private Bill createTestBill() {
		Patient patient = patientService.getPatient(0);
		
		BillableService billableService = new BillableService();
		billableService.setName("Consultation");
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setBillableService(billableService);
		lineItem.setPrice(new BigDecimal("100.00"));
		lineItem.setQuantity(1);
		
		Bill bill = new Bill();
		bill.setPatient(patient);
		bill.setCashier(providerService.getProvider(0));
		bill.setReceiptNumber("RCP-001");
		bill.setDateCreated(new Date());
		bill.addLineItem(lineItem);
		bill.setPayments(new HashSet<>());
		
		return bill;
	}
	
	private String extractTextFromPdf(byte[] pdfBytes) throws Exception {
		try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)))) {
			StringBuilder text = new StringBuilder();
			for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
				text.append(PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i)));
			}
			return text.toString();
		}
	}
	
	private String getCurrencySymbol(Locale locale) {
		NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
		if (nf instanceof DecimalFormat) {
			return ((DecimalFormat) nf).getDecimalFormatSymbols().getCurrencySymbol();
		}
		return nf.getCurrency().getSymbol(locale);
	}
}
