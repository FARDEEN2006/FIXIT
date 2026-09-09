import React, { useRef, useState } from "react";
import { Camera, Image as ImageIcon } from "lucide-react";
import { createSecondHandListing } from "../services/api";
import "../styles/sell.css";
import SEO from "../components/common/SEO";

const initial = { sellerName: "", sellerPhone: "", sellerEmail: "", productName: "", condition: "", detailedDescription: "", expectedPrice: "" };

const MAX_IMAGES = 4;
const MAX_IMAGE_DIMENSION = 1600;
const TARGET_SIZE = 1024 * 1024;

function formatSize(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function compressImage(file) {
  return new Promise((resolve, reject) => {
    const image = new Image();
    const objectUrl = URL.createObjectURL(file);
    image.onload = () => {
      URL.revokeObjectURL(objectUrl);
      const scale = Math.min(1, MAX_IMAGE_DIMENSION / Math.max(image.naturalWidth, image.naturalHeight));
      const canvas = document.createElement("canvas");
      canvas.width = Math.max(1, Math.round(image.naturalWidth * scale));
      canvas.height = Math.max(1, Math.round(image.naturalHeight * scale));
      const context = canvas.getContext("2d");
      if (!context) {
        reject(new Error("Your browser could not prepare this image."));
        return;
      }
      context.fillStyle = "#fff";
      context.fillRect(0, 0, canvas.width, canvas.height);
      context.drawImage(image, 0, 0, canvas.width, canvas.height);

      const createBlob = (quality) => canvas.toBlob((blob) => {
        if (!blob) {
          reject(new Error(`Could not compress ${file.name || "the selected image"}.`));
          return;
        }
        if (blob.size <= TARGET_SIZE || quality <= 0.72) {
          resolve(new File([blob], `${file.name.replace(/\.[^/.]+$/, "") || "photo"}.jpg`, { type: "image/jpeg", lastModified: Date.now() }));
          return;
        }
        createBlob(quality - 0.04);
      }, "image/jpeg", quality);
      createBlob(0.8);
    };
    image.onerror = () => {
      URL.revokeObjectURL(objectUrl);
      reject(new Error(`${file.name || "A selected image"} could not be read.`));
    };
    image.src = objectUrl;
  });
}

export default function SellYourMobile() {
  const [form, setForm] = useState(initial);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const [images, setImages] = useState([]);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [photoStatuses, setPhotoStatuses] = useState([]);
  const galleryInput = useRef(null);
  const cameraInput = useRef(null);

  const selectImages = async (event) => {
    const selected = Array.from(event.target.files || []);
    event.target.value = "";
    if (!selected.length) return;
    if (images.length + selected.length > MAX_IMAGES) {
      setError(`You can select up to ${MAX_IMAGES} photos. ${MAX_IMAGES - images.length} slot(s) remaining.`);
      return;
    }
    setError(""); setMessage("");
    try {
      const optimized = await Promise.all(selected.map(async (file) => ({
        file: await compressImage(file),
        originalSize: file.size,
      })));
      const prepared = optimized.map((image) => ({ ...image, previewUrl: URL.createObjectURL(image.file) }));
      setImages((currentImages) => [...currentImages, ...prepared]);
      setPhotoStatuses((currentStatuses) => [...currentStatuses, ...prepared.map(() => "pending")]);
    } catch (err) {
      setError(err.message || "We could not optimize the selected photos. Please try again.");
    }
  };

  const submit = async (event) => {
    event.preventDefault(); setError(""); setMessage("");
    if (!/^\+?[0-9]{10,15}$/.test(form.sellerPhone.replace(/[\s()-]/g, ""))) return setError("Enter a valid phone number including country code if needed.");
    setSaving(true);
    setUploadProgress(0);
    setPhotoStatuses(images.map(() => "pending"));
    try {
      if (images.length < 1 || images.length > 4) throw new Error("Please select between 1 and 4 images.");
      await createSecondHandListing(
        { ...form, sellerPhone: form.sellerPhone.replace(/[\s()-]/g, "") },
        images.map(({ file }) => file),
        (progress) => {
          setUploadProgress(progress);
          setPhotoStatuses(images.map((_, index) => {
            const completedBefore = Math.floor((progress / 100) * images.length);
            return index < completedBefore ? "done" : index === completedBefore ? "uploading" : "pending";
          }));
        },
      );
      setUploadProgress(100); setPhotoStatuses(images.map(() => "done"));
      setMessage("Your private listing has been submitted for review."); setForm(initial); images.forEach(({ previewUrl }) => URL.revokeObjectURL(previewUrl)); setImages([]);
    } catch (err) { setError(err.message || "We could not submit your listing."); setUploadProgress(0); setPhotoStatuses(images.map(() => "pending")); }
    finally { setSaving(false); }
  };
  return <main className="section"><SEO title="Sell Your Mobile in Karur" path="/sell-your-mobile" description="Sell your mobile phone in Karur through FIXIT Mobile Sales & Services." /><div className="container"><div className="section-header"><span className="section-label">Private listing</span><h1>Sell Your Mobile</h1><p>Share your device details and photos. Your listing stays private and is reviewed by FIXIT.</p></div>
    <form className="fixit-sell-form" onSubmit={submit}>
      {[['sellerName','Your name','text'],['sellerPhone','Phone number','tel'],['sellerEmail','Email address (optional)','email'],['productName','Mobile / product name','text'],['expectedPrice','Expected price','number']].map(([name,label,type]) => <label key={name}>{label}<input required={name !== 'sellerEmail'} name={name} type={type} min={type === 'number' ? '1' : undefined} value={form[name]} onChange={e=>setForm({...form,[name]:e.target.value})}/></label>)}
      <label>Condition<select required value={form.condition} onChange={e=>setForm({...form,condition:e.target.value})}><option value="">Select condition</option><option value="NEW">New (With Warranty)</option><option value="GOOD">Good (No Damage)</option><option value="FAIR">Fair (With Damage But Working Condition)</option><option value="POOR">Poor (Damage + No Working Condition)</option></select></label>
      <label className="fixit-sell-full">Detailed description<textarea required minLength="20" maxLength="2000" value={form.detailedDescription} onChange={e=>setForm({...form,detailedDescription:e.target.value})} placeholder="Tell us about the device, storage, accessories, faults or repairs." /></label>
      <label className="fixit-sell-full">Device photos (1–4)<span className="fixit-image-upload-tip" role="note">Photos are optimized securely in your browser before upload. Choose up to 4 photos from your gallery or take photos with your camera.</span><input ref={galleryInput} type="file" accept="image/*" multiple onChange={selectImages} /><input ref={cameraInput} type="file" accept="image/*" capture="environment" onChange={selectImages} /><div className="fixit-photo-actions"><button type="button" className="fixit-button fixit-photo-button" onClick={() => galleryInput.current?.click()} disabled={saving || images.length >= MAX_IMAGES}><ImageIcon size={18} aria-hidden="true" />Upload Photos</button><button type="button" className="fixit-button fixit-camera-button" onClick={() => cameraInput.current?.click()} disabled={saving || images.length >= MAX_IMAGES} aria-label="Take photo with camera" title="Take photo with camera"><Camera size={20} aria-hidden="true" /></button></div><small>MOBILES: FRONT, BACK, TOP, BOTTOM</small><small>JPG, JPEG, JFIF, PNG and WebP supported.</small><small>{images.length} selected; the first is the main image.</small></label>
      {images.length > 0 && <div className="fixit-image-preview-grid">{images.map(({ file, originalSize, previewUrl }, index) => <figure key={`${file.name}-${index}`}><img src={previewUrl} alt={`Selected mobile photo ${index + 1}`} /><figcaption><strong>Photo {index + 1}</strong><span>Original: {formatSize(originalSize)}</span><span>Optimized: {formatSize(file.size)}</span>{saving && <span>{photoStatuses[index] === "done" ? "✓ Uploaded" : photoStatuses[index] === "uploading" ? "⏳ Uploading" : "○ Waiting"}</span>}</figcaption></figure>)}</div>}
      {error && <p className="fixit-form-error" role="alert">{error}</p>}{message && <p className="fixit-form-success" role="status">{message}</p>}
      {saving && <div className="fixit-upload-progress" role="status"><div className="fixit-upload-progress-label"><span>Uploading photos…</span><strong>{uploadProgress}%</strong></div><progress value={uploadProgress} max="100" /><small>Please don't close this page.</small></div>}
      <button disabled={saving || !images.length} className="fixit-button fixit-button-primary">{saving ? "Uploading…" : "Continue & Upload"}</button>
    </form></div></main>;
}
