package br.com.xavier.pdf.pdf.watermark;

import java.io.IOException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfStamper;

import java.util.LinkedHashSet;

import com.itextpdf.text.pdf.PdfReader;

public class PdfWatermarker {
	
	private static final String digitalSignatureWatermarkText = new String("DOCUMENTO ASSINADO DIGITALMENTE");
	private static final int a4WatermarkFontSize = 49;
	private static BaseFont watermarkFont;
	
	private static BaseFont getWaterkmarkFont() throws DocumentException, IOException{
		if(watermarkFont == null){
			watermarkFont = BaseFont.createFont("/oracle/forms/aptools/pdf/resources/font/arial.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
		}
		return watermarkFont;
	}
	
	public static void applyWatermarkOnAllPages(
		PdfReader reader, PdfStamper stamper, String text, BaseColor color, float opacity
	) throws DocumentException, IOException {
		
		int numberOfPages = reader.getNumberOfPages();
		
		PdfGState gs = new PdfGState();
		gs.setFillOpacity(opacity);

		for (int i = 1; i <= numberOfPages; i++) {
			Rectangle pageSize = reader.getPageSize(i);
			int pageRotation = reader.getPageRotation(i);
			
			boolean isLandscape = false;
			if(pageRotation > 0){
				isLandscape = true;
			}
			
			int pageDiagonalSize = getDiagonalSize(pageSize);
			int watermarkAngle = getDiagonalAngle(pageSize, pageDiagonalSize, isLandscape);
			
			LinkedHashSet<Integer> fontSizeSet = new LinkedHashSet<Integer>();
			float fontSize = determineFontSize(pageDiagonalSize, a4WatermarkFontSize, fontSizeSet);
			fontSize = fontSize - 1;
			
			int cornerPlacement = getCornerPlacementDiagonal(fontSize);
			
			PdfContentByte underContent = stamper.getUnderContent(i);
			underContent.setGState(gs);
			underContent.setFontAndSize(getWaterkmarkFont(), fontSize);
			underContent.setColorFill(color);
			underContent.beginText();
			underContent.showTextAligned(
				Element.ALIGN_BASELINE, text, 
				cornerPlacement/2,
				cornerPlacement/2,
				watermarkAngle
			);
			underContent.endText();
		}
	}
	
	private static int getDiagonalSize(Rectangle pageSize){
		double sum = Math.pow(pageSize.getWidth(), 2) + Math.pow(pageSize.getHeight(), 2);
		double result = Math.sqrt(sum);
		return (int) result;
	}
	
	//TODO rever como programar o desconto de 2graus
	private static int getDiagonalAngle(Rectangle pageSize, int pageDiagonalSize, boolean isLandscape){
		if(isLandscape){
			return (int) Math.toDegrees(Math.asin(pageSize.getWidth() / pageDiagonalSize)) - 2;
		} else {
			return (int) Math.toDegrees(Math.asin(pageSize.getHeight() / pageDiagonalSize));
		}
	}
	
	private static int getCornerPlacementDiagonal(float estimatedFontSize) throws DocumentException, IOException{
		float fontAscent = getWaterkmarkFont().getFontDescriptor(BaseFont.ASCENT, estimatedFontSize); 
		float fontDescent = getWaterkmarkFont().getFontDescriptor(BaseFont.DESCENT, estimatedFontSize);
		int height = (int) (fontAscent - fontDescent);
		
		int firstLetterWidthPoints = (int) getWaterkmarkFont().getWidthPoint(digitalSignatureWatermarkText.substring(0, 1), estimatedFontSize);
		int width = (int) Math.sqrt(Math.pow(firstLetterWidthPoints, 2) + Math.pow(height, 2));
		
		Rectangle r = new Rectangle(width, height);
		int fontRectangleDiagonalSize = getDiagonalSize(r);
		
		r = new Rectangle(fontRectangleDiagonalSize, fontRectangleDiagonalSize);
		int diagonalSize =  getDiagonalSize(r);
		return diagonalSize;
	}
	
	private static int determineFontSize(
		int pageDiagonalSize, int estimatedFontSize, LinkedHashSet<Integer> haltSet
	) throws DocumentException, IOException{
		
		if(haltSet.contains(estimatedFontSize)){
			return estimatedFontSize;
		}
		
		int newPageDiagonalSize = pageDiagonalSize - getCornerPlacementDiagonal(estimatedFontSize);
		int a4WaterMarkSizePoints = (int) getWaterkmarkFont().getWidthPoint(digitalSignatureWatermarkText, estimatedFontSize);
		
		if(a4WaterMarkSizePoints > newPageDiagonalSize){
			haltSet.add(estimatedFontSize);
			return determineFontSize(pageDiagonalSize, (estimatedFontSize - 1), haltSet);
		} else if(a4WaterMarkSizePoints < newPageDiagonalSize) {
			haltSet.add(estimatedFontSize);
			return determineFontSize(pageDiagonalSize, (estimatedFontSize + 1), haltSet);
		} else {
			return a4WaterMarkSizePoints;
		}
	}
}