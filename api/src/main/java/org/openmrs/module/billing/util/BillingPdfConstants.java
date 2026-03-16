package org.openmrs.module.billing.util;

public interface BillingPdfConstants {
	
	String GP_BILL_LOGO_PATH = "billing.receipt.logoPath";
	
	int PAGE_WIDTH_PTS = 288;
	
	int PAGE_HEIGHT_PTS = 720;
	
	int FONT_SIZE_NORMAL = 12;
	
	int FONT_SIZE_SMALL = 10;
	
	int FONT_SIZE_FOOTER = 8;
	
	float[] HEADER_COL_WIDTHS = { 2f, 7f };
	
	float[] BILL_COL_WIDTHS = { 1f, 5f, 2f, 2f };
	
	float[] TOTALS_COL_WIDTHS = { 1f, 5f, 2f, 2f };
	
	float[] PAYMENT_COL_WIDTHS = { 1f, 5f, 2f, 2f };
	
	int BILLING_RECEIPT_PDF_TOP_MARGIN = 6;
	
	int BILLING_RECEIPT_PDF_RIGHT_MARGIN = 12;
	
	int BILLING_RECEIPT_PDF_BOTTOM_MARGIN = 2;
	
	int BILLING_RECEIPT_PDF_LEFT_MARGIN = 12;
	
	String DIVIDER = "------------------------------------------------------------------";
}
