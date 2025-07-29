import kagglehub
GEMMA_PATH = kagglehub.model_download("google/gemma-3n/transformers/gemma-3n-e2b")
from transformers import AutoProcessor, AutoModelForImageTextToText
processor = AutoProcessor.from_pretrained(GEMMA_PATH)
model = AutoModelForImageTextToText.from_pretrained(GEMMA_PATH, torch_dtype="auto", device_map="auto")
prompt = """It was a dark and stormy night. """
input_ids = processor(text=prompt, return_tensors="pt").to(model.device, dtype=model.dtype)
outputs = model.generate(**input_ids, max_new_tokens=512, disable_compile=True)
text = processor.batch_decode(
    outputs,
    skip_special_tokens=False,
    clean_up_tokenization_spaces=False
)
print(text[0])
