"""
FastAPI backend for document analysis with domain-specific NLP models.
Supports OCR, text extraction, and specialized analysis for finance, healthcare, and legal domains.
"""

import os
import tempfile
import time
import logging
from typing import List, Dict, Any, Optional
from pathlib import Path

from fastapi import FastAPI, File, Form, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

# Import our custom modules
from ocr_utils import extract_text_from_file
from nlp_models import DomainAnalyzer, AnalysisResult

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize FastAPI app
app = FastAPI(
    title="Document Analysis API",
    description="AI-powered document analysis for finance, healthcare, and legal domains",
    version="1.0.0"
)

# Add CORS middleware to allow Spring Boot backend to call this service
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify your Spring Boot server URL
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize domain analyzer (loads all NLP models)
domain_analyzer = DomainAnalyzer()

# Pydantic models for API responses
class FileInfo(BaseModel):
    fileName: str
    fileSize: int
    fileType: str

class Issue(BaseModel):
    type: str
    detail: str

class Analysis(BaseModel):
    summary: str
    due_diligence: str
    confidence: float
    issues: List[Issue]

class AnalysisResponse(BaseModel):
    fileInfo: FileInfo
    domain: str
    analysis: Analysis

@app.post("/analyze", response_model=AnalysisResponse)
async def analyze_document(
    files: UploadFile = File(..., description="Document file (PDF, DOCX, TXT)"),
    domain: str = Form(..., description="Analysis domain: finance, healthcare, or legal")
):
    """
    Main endpoint for document analysis.
    Accepts a file and domain, extracts text, and performs specialized NLP analysis.
    """
    start_time = time.time()
    
    try:
        # Validate domain
        if domain.lower() not in ["finance", "healthcare", "legal"]:
            raise HTTPException(
                status_code=400, 
                detail="Invalid domain. Supported domains: finance, healthcare, legal"
            )
        
        # Validate file type
        allowed_types = {
            "application/pdf": [".pdf"],
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document": [".docx"],
            "application/msword": [".doc"],
            "text/plain": [".txt"]
        }
        
        file_extension = Path(files.filename).suffix.lower()
        content_type = files.content_type
        
        # Check if file type is supported
        type_supported = False
        for mime_type, extensions in allowed_types.items():
            if content_type == mime_type or file_extension in extensions:
                type_supported = True
                break
        
        if not type_supported:
            raise HTTPException(
                status_code=400,
                detail=f"Unsupported file type: {content_type}. Supported types: PDF, DOCX, DOC, TXT"
            )
        
        logger.info(f"Processing file: {files.filename} ({content_type}) for domain: {domain}")
        
        # Create temporary file to store upload
        with tempfile.NamedTemporaryFile(delete=False, suffix=file_extension) as temp_file:
            # Read and save uploaded file
            content = await files.read()
            temp_file.write(content)
            temp_file_path = temp_file.name
        
        try:
            # Extract text from the document using OCR/text extraction
            logger.info("Extracting text from document...")
            extracted_text = extract_text_from_file(temp_file_path, file_extension)
            
            if not extracted_text or len(extracted_text.strip()) < 10:
                raise HTTPException(
                    status_code=400,
                    detail="Could not extract sufficient text from the document. Please ensure the document contains readable text."
                )
            
            logger.info(f"Extracted {len(extracted_text)} characters of text")
            
            # Perform domain-specific analysis
            logger.info(f"Performing {domain} domain analysis...")
            analysis_result = domain_analyzer.analyze(extracted_text, domain.lower())
            
            # Create file info
            file_info = FileInfo(
                fileName=files.filename,
                fileSize=len(content),
                fileType=content_type
            )
            
            # Create analysis response
            analysis = Analysis(
                summary=analysis_result.summary,
                due_diligence=analysis_result.due_diligence,
                confidence=analysis_result.confidence,
                issues=[Issue(type=issue["type"], detail=issue["detail"]) for issue in analysis_result.issues]
            )
            
            processing_time = time.time() - start_time
            logger.info(f"Analysis completed in {processing_time:.2f} seconds")
            
            return AnalysisResponse(
                fileInfo=file_info,
                domain=domain,
                analysis=analysis
            )
            
        finally:
            # Clean up temporary file
            if os.path.exists(temp_file_path):
                os.unlink(temp_file_path)
                
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error processing document: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Internal server error during document processing: {str(e)}"
        )

@app.get("/health")
async def health_check():
    """Health check endpoint for service monitoring."""
    return {
        "status": "healthy",
        "service": "Document Analysis API",
        "version": "1.0.0",
        "supported_domains": ["finance", "healthcare", "legal"]
    }

@app.get("/domains")
async def get_supported_domains():
    """Get list of supported analysis domains."""
    return {
        "domains": ["finance", "healthcare", "legal"],
        "models_loaded": domain_analyzer.get_loaded_models()
    }

@app.get("/")
async def root():
    """Root endpoint with API information."""
    return {
        "message": "Document Analysis API",
        "docs_url": "/docs",
        "health_check": "/health",
        "supported_domains": "/domains"
    }

if __name__ == "__main__":
    import uvicorn
    
    # Run the FastAPI server
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=5000,
        reload=True,
        log_level="info"
    )