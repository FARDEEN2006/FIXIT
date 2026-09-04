import React, { useState } from "react";
import { createSecondHandListing } from "../services/api";
import "../styles/sell.css";

const initial = { sellerName: "", sellerPhone: "", sellerEmail: "", productName: "", condition: "", detailedDescription: "", expectedPrice: "" };

export default function SellYourMobile() {
  const [form, setForm] = useState(initial);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const [images, setImages] = useState([]);
  const submit = async (event) => {
    event.preventDefault(); setError(""); setMessage("");
    if (!/^\+?[0-9]{10,15}$/.test(form.sellerPhone.replace(/[\s()-]/g, ""))) return setError("Enter a valid phone number including country code if needed.");
    setSaving(true);
    try {
      if (images.length < 1 || images.length > 4) throw new Error("Please select between 1 and 4 images.");
      await createSecondHandListing({ ...form, sellerPhone: form.sellerPhone.replace(/[\s()-]/g, "") }, images);
      setMessage("Your private listing has been submitted for review."); setForm(initial); setImages([]);
    } catch (err) { setError(err.message || "We could not submit your listing."); }
    finally { setSaving(false); }
  };
  return <main className="section"><div className="container"><div className="section-header"><span className="section-label">Private listing</span><h1>Sell Your Mobile</h1><p>Share your device details and photos. Your listing stays private and is reviewed by FIXIT.</p></div>
    <form className="fixit-sell-form" onSubmit={submit}>
      {[['sellerName','Your name','text'],['sellerPhone','Phone number','tel'],['sellerEmail','Email address (optional)','email'],['productName','Mobile / product name','text'],['expectedPrice','Expected price','number']].map(([name,label,type]) => <label key={name}>{label}<input required={name !== 'sellerEmail'} name={name} type={type} min={type === 'number' ? '1' : undefined} value={form[name]} onChange={e=>setForm({...form,[name]:e.target.value})}/></label>)}
      <label>Condition<select required value={form.condition} onChange={e=>setForm({...form,condition:e.target.value})}><option value="">Select condition</option><option value="NEW">New</option><option value="GOOD">Good</option><option value="FAIR">Fair</option><option value="POOR">Poor</option></select></label>
      <label className="fixit-sell-full">Detailed description<textarea required minLength="20" maxLength="2000" value={form.detailedDescription} onChange={e=>setForm({...form,detailedDescription:e.target.value})} placeholder="Tell us about the device, storage, accessories, faults or repairs." /></label>
      <label className="fixit-sell-full">Device photos (1–4)<input required type="file" accept="image/jpeg,image/png,image/webp" multiple onChange={e=>{const selected=Array.from(e.target.files); if(selected.length>4){setImages([]);setError("A maximum of 4 images is allowed.");e.target.value="";}else{setError("");setImages(selected);}}}/><small>{images.length} selected; the first is the main image.</small></label>
      {error && <p className="fixit-form-error" role="alert">{error}</p>}{message && <p className="fixit-form-success" role="status">{message}</p>}
      <button disabled={saving} className="fixit-button fixit-button-primary">{saving ? "Submitting…" : "Submit private listing"}</button>
    </form></div></main>;
}
