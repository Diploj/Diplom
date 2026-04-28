import cv2
import numpy as np
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
import insightface
from insightface.app import FaceAnalysis
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Face Embedding Extractor")


face_app = FaceAnalysis(name='buffalo_l', providers=['CPUExecutionProvider'])

face_app.prepare(ctx_id=0, det_size=(640, 640))

@app.post("/extract-embeddings", response_class=JSONResponse)
async def extract_embeddings(file: UploadFile = File(...)):
    """
    Принимает изображение, возвращает список эмбеддингов для обнаруженных лиц.
    Формат ответа: {"embeddings": [[float, ...], ...]}
    """
    if not file.content_type.startswith('image/'):
        raise HTTPException(status_code=400, detail="File must be an image")

    try:
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if img is None:
            raise HTTPException(status_code=400, detail="Invalid image")
    except Exception as e:
        logger.error(f"Error reading image: {e}")
        raise HTTPException(status_code=400, detail="Could not read image")

    try:
        faces = face_app.get(img)
    except Exception as e:
        logger.error(f"Error during face detection: {e}")
        raise HTTPException(status_code=500, detail="Face detection failed")

    if not faces:
        return {"embeddings": []}

    embeddings = [face.embedding.tolist() for face in faces]

    logger.info(f"Found {len(embeddings)} faces")
    return {"embeddings": embeddings}

@app.get("/health")
async def health():
    return {"status": "ok"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)