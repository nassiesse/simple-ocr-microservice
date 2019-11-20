package com.nassiesse.ocr.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@RestController
public class SimpleOCRController {

	@PostMapping("/api/pdf/extractText")
    public @ResponseBody ResponseEntity<String> 
					extractTextFromPDFFile(@RequestParam("file") MultipartFile file) {
		try {
			
			// Load file into PDFBox class
			PDDocument document = PDDocument.load(file.getBytes());
			PDFTextStripper stripper = new PDFTextStripper();
			String strippedText = stripper.getText(document);
			
			// Check text exists into the file
			if (strippedText.trim().isEmpty()){
				strippedText = extractTextFromScannedDocument(document);
			}
			
			JSONObject obj = new JSONObject();
	        obj.put("fileName", file.getOriginalFilename());
	        obj.put("text", strippedText.toString());
			
			return new ResponseEntity<String>(obj.toString(), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@GetMapping("/api/pdf/ping")
    public ResponseEntity<String> get()
    {
		return new ResponseEntity<String>("PONG", HttpStatus.OK);
    }

	private String extractTextFromScannedDocument(PDDocument document) throws IOException, TesseractException {
		
		// Extract images from file
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		StringBuilder out = new StringBuilder();
		
		ITesseract _tesseract = new Tesseract();
		_tesseract.setDatapath("/usr/share/tessdata/");
		_tesseract.setLanguage("ita"); // choose your language
				
		for (int page = 0; page < document.getNumberOfPages(); page++)
		{ 
		    BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
		    
		    // Create a temp image file
    	    File temp = File.createTempFile("tempfile_" + page, ".png"); 
    	    ImageIO.write(bim, "png", temp);
	        
    	    String result = _tesseract.doOCR(temp);
		    out.append(result);
		
		    // Delete temp file
		    temp.delete();
		    
		}
		
		return out.toString();

	}
	
	
	
}

